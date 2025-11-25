package com.aqua.guard.monitoramento.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "leituras_volume")
@Entity(name = "LeituraVolume")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeituraVolume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caixa_dagua_id", nullable = false)
    private CaixaDAgua caixaDAgua;

    @Column(name = "volume_litros", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumeLitros;

    @CreationTimestamp
    @Column(name = "data_hora_leitura", nullable = false, updatable = false)
    private LocalDateTime dataHoraLeitura;

    public LeituraVolume(CaixaDAgua caixa, BigDecimal volume) {
        this.caixaDAgua = caixa;
        this.volumeLitros = volume;
    }
}