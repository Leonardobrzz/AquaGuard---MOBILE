package com.aqua.guard.monitoramento.core.service;

import com.aqua.guard.monitoramento.core.entity.DispositivoUsuario;
import com.aqua.guard.monitoramento.core.entity.Usuario;
import com.aqua.guard.monitoramento.core.persistence.DispositivoUsuarioEC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DispositivoUsuarioAS {

    @Autowired
    private DispositivoUsuarioEC dispositivoUsuarioEC;

    public void registrarDispositivo(Usuario usuario, String pushToken) {
        var dispositivoOpt = dispositivoUsuarioEC.findByPushToken(pushToken);

        if (dispositivoOpt.isEmpty()) {
            var novoDispositivo = new DispositivoUsuario(usuario, pushToken);
            dispositivoUsuarioEC.save(novoDispositivo);
        }
    }

    @Transactional
    public void excluirPorToken(String token) {
        dispositivoUsuarioEC.findByPushToken(token).ifPresent(dispositivo -> {
            dispositivoUsuarioEC.delete(dispositivo);
        });
    }

}