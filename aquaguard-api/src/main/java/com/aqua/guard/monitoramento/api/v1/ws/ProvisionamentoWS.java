package com.aqua.guard.monitoramento.api.v1.ws;

import com.aqua.guard.monitoramento.api.v1.dto.ConfiguracaoDispositivoDTO;
import com.aqua.guard.monitoramento.core.service.ProvisionamentoAS;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/provisionamento")
public class ProvisionamentoWS {

    @Autowired
    private ProvisionamentoAS provisionamentoAS;

    @GetMapping("/configuracao/{serialNumber}")
    public ResponseEntity<ConfiguracaoDispositivoDTO> getConfiguracao(@PathVariable String serialNumber) {
        try {
            var config = provisionamentoAS.obterConfiguracao(serialNumber);
            return ResponseEntity.ok(config);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}