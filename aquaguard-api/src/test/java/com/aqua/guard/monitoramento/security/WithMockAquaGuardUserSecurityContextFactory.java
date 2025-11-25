package com.aqua.guard.monitoramento.security;

import com.aqua.guard.monitoramento.core.entity.Usuario;
import com.aqua.guard.monitoramento.core.persistence.UsuarioEC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

@Component
public class WithMockAquaGuardUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockAquaGuardUser> {

    @Autowired
    private UsuarioEC usuarioEC;

    @Override
    public SecurityContext createSecurityContext(WithMockAquaGuardUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Usuario principal = usuarioEC.findByEmail(annotation.username())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + annotation.username()));

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        context.setAuthentication(authToken);
        return context;
    }
}