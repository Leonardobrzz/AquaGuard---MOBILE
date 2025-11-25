package com.aqua.guard.monitoramento.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockAquaGuardUserSecurityContextFactory.class)
public @interface WithMockAquaGuardUser {

    String username() default "teste@aquaguard.com";
}