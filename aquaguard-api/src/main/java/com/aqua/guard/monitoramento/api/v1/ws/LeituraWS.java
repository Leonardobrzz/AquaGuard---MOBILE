package com.aqua.guard.monitoramento.api.v1.ws;

import com.aqua.guard.monitoramento.api.v1.dto.LeituraEmbarcadoDTO;
import com.aqua.guard.monitoramento.core.entity.CaixaDAgua;
import com.aqua.guard.monitoramento.core.service.LeituraAS;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leituras")
public class LeituraWS {

    @Autowired
    private LeituraAS leituraAS;

    @PostMapping
    @Transactional
    public ResponseEntity<Void> registrarLeitura(
            @RequestBody @Valid LeituraEmbarcadoDTO dados,
            @AuthenticationPrincipal CaixaDAgua caixaAutenticada
    ) {
        leituraAS.salvarLeitura(caixaAutenticada, dados.volumeLitros());
        return ResponseEntity.noContent().build();
    }
}