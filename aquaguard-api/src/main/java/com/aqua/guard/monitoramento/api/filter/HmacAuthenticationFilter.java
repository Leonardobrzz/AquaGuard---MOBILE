package com.aqua.guard.monitoramento.api.filter;

import com.aqua.guard.monitoramento.core.persistence.CaixaDAguaEC;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HmacAuthenticationFilter extends OncePerRequestFilter {

    private final CaixaDAguaEC caixaDAguaEC;

    public HmacAuthenticationFilter(CaixaDAguaEC caixaDAguaEC) {
        this.caixaDAguaEC = caixaDAguaEC;
    }

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long MAX_REQUEST_AGE_IN_SECONDS = 300; // 5 minutos

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String path = request.getRequestURI();

        if (!path.startsWith("/api/v1/leituras") && !path.startsWith("/api/v1/provisionamento")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String receivedSignature = getSignatureFromHeader(request);
            String timestampStr = request.getHeader("X-Timestamp");
            String apiKey = request.getHeader("X-API-Key");

            if (receivedSignature == null || timestampStr == null || apiKey == null) {
                throw new SecurityException("Cabeçalhos de autenticação ausentes");
            }

            validateTimestamp(timestampStr);

            var caixaOptional = caixaDAguaEC.findByChaveApi(apiKey);
            if (caixaOptional.isEmpty()) {
                throw new SecurityException("Chave de API inválida");
            }
            String secretKey = caixaOptional.get().getChaveApi();

            String dataToSign = buildDataToSign(request);
            String calculatedSignature = calculateHmac(secretKey, dataToSign);

            if (!receivedSignature.equals(calculatedSignature)) {
                System.err.println(">>> FALHA HMAC: Assinatura inválida.");
                System.err.println("   - Recebida:  " + receivedSignature);
                System.err.println("   - Calculada: " + calculatedSignature);
                System.err.println("   - Dados:     \"" + dataToSign + "\"");
                throw new SecurityException("Assinatura inválida");
            }

            System.out.println(">>> SUCESSO HMAC: Assinatura válida para " + path);
            var caixa = caixaOptional.get();
            var authentication = new UsernamePasswordAuthenticationToken(caixa, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            System.err.println(">>> ERRO NO FILTRO HMAC: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String buildDataToSign(HttpServletRequest request) {
        String timestamp = request.getHeader("X-Timestamp");
        String path = request.getRequestURI();
        return path + "." + timestamp;
    }

    private void validateTimestamp(String timestampStr) {
        try {
            long requestTimestamp = Long.parseLong(timestampStr);
            long currentTimestamp = System.currentTimeMillis() / 1000L;
            if (Math.abs(currentTimestamp - requestTimestamp) > MAX_REQUEST_AGE_IN_SECONDS) {
                throw new SecurityException("Requisição fora da janela de tempo permitida.");
            }
        } catch (NumberFormatException e) {
            throw new SecurityException("Timestamp em formato inválido.");
        }
    }

    private String calculateHmac(String secret, String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular HMAC", e);
        }
    }

    private String getSignatureFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}