package com.aqua.guard.monitoramento.api.filter;

import com.aqua.guard.monitoramento.api.exception.InvalidTokenException;
import com.aqua.guard.monitoramento.core.service.TokenAS;
import com.aqua.guard.monitoramento.core.persistence.UsuarioEC;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenAS tokenAS;

    private final UsuarioEC usuarioEC;

    private final AuthenticationEntryPoint authEntryPoint;

    public SecurityFilter(TokenAS tokenAS, UsuarioEC usuarioEC, @Qualifier("customAuthenticationEntryPoint") AuthenticationEntryPoint authEntryPoint) {
        this.tokenAS = tokenAS;
        this.usuarioEC = usuarioEC;
        this.authEntryPoint = authEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Pular endpoints públicos que não precisam de autenticação JWT
        String requestPath = request.getRequestURI();
        System.out.println("SecurityFilter: Processando path: " + requestPath);
        
        if (isPublicEndpoint(requestPath)) {
            System.out.println("SecurityFilter: Path é público, pulando autenticação: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var tokenJWT = recuperarToken(request);
            if (tokenJWT != null) {
                var subject = tokenAS.getSubject(tokenJWT);
                var usuario = usuarioEC.findByEmail(subject).orElse(null);
                if (usuario != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                }
            }
            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            authEntryPoint.commence(request, response, e);
        }
    }

    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }

    private boolean isPublicEndpoint(String requestPath) {
        return requestPath.startsWith("/api/v1/auth/") ||
               requestPath.startsWith("/api/v1/debug/") ||
               requestPath.startsWith("/api/auth/") ||
               requestPath.startsWith("/h2-console") ||
               requestPath.equals("/api/v1/caixas-dagua/gerar-serial-teste");
    }
}