package com.aqua.guard.monitoramento.core.persistence;

import com.aqua.guard.monitoramento.core.entity.CaixaDAgua;
import com.aqua.guard.monitoramento.core.entity.LeituraVolume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LeituraVolumeEC extends JpaRepository<LeituraVolume, Long> {

    Optional<LeituraVolume> findFirstByCaixaDAguaOrderByDataHoraLeituraDesc(CaixaDAgua caixa);

    List<LeituraVolume> findAllByCaixaDAguaAndDataHoraLeituraBetweenOrderByDataHoraLeituraAsc(
            CaixaDAgua caixa, LocalDateTime inicio, LocalDateTime fim);

}