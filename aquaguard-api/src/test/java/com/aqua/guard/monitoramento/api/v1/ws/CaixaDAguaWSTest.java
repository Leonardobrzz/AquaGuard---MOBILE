package com.aqua.guard.monitoramento.api.v1.ws;

import com.aqua.guard.monitoramento.security.WithMockAquaGuardUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockAquaGuardUser
public class CaixaDAguaWSTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarDadosDeAnaliseParaPeriodoValido() throws Exception {
        String idCaixaDAgua = "0a7d8b9f-3c1d-4e0a-99b3-5e3e2f7d1a2b";
        String dataInicio = "2025-06-28T09:00:00";
        String dataFim = "2025-06-29T09:00:00";

        mockMvc.perform(get("/caixas-dagua/{id}/analise", idCaixaDAgua)
                        .param("inicio", dataInicio)
                        .param("fim", dataFim))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pontosDoGrafico").isArray())
                .andExpect(jsonPath("$.pontosDoGrafico.length()").value(3))
                .andExpect(jsonPath("$.pontosDoGrafico[0].volume").value(950.00));
    }

    @Test
    void deveRetornarNaoEncontradoParaIdInexistente() throws Exception {
        String idInexistente = "11111111-1111-1111-1111-111111111111";
        String dataInicio = "2025-06-28T09:00:00";
        String dataFim = "2025-06-29T09:00:00";

        mockMvc.perform(get("/caixas-dagua/{id}/analise", idInexistente)
                        .param("inicio", dataInicio)
                        .param("fim", dataFim))
                .andExpect(status().isNotFound());
    }
}