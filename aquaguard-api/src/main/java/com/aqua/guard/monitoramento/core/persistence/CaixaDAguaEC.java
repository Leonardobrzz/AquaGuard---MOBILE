package com.aqua.guard.monitoramento.core.persistence;

import com.aqua.guard.monitoramento.core.entity.CaixaDAgua;
import com.aqua.guard.monitoramento.core.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaixaDAguaEC extends JpaRepository<CaixaDAgua, UUID> {

    Optional<CaixaDAgua> findBySerialNumber(String serialNumber);

    Optional<CaixaDAgua> findByChaveApi(String chaveApi);

    List<CaixaDAgua> findAllByUsuario(Usuario usuario);

    @Query("SELECT c FROM CaixaDAgua c WHERE c.usuario = :usuario")
    List<CaixaDAgua> findAllByUsuarioIgnoringWhere(Usuario usuario);

}
