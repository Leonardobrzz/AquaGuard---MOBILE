package com.aqua.guard.monitoramento.core.persistence;

import com.aqua.guard.monitoramento.core.entity.DispositivoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DispositivoUsuarioEC extends JpaRepository<DispositivoUsuario, Long> {

    Optional<DispositivoUsuario> findByPushToken(String pushToken);

}