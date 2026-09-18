package com.ovg.transportes.model;

import com.ovg.transportes.model.Cidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ponto_rota")
public class PontoRota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rota_id", nullable = false)
    private Rota rota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidade_id", nullable = false)
    private Cidade cidade;

    // preenchido so quando o ponto veio de uma unidade da OVG (nao de uma
    // cidade "outro local") — da precisao na exibicao quando origem e destino
    // sao unidades diferentes na mesma cidade (ex.: SEDE -> CISF).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_administrativo_id")
    private LocalAdministrativo local;

    @Column(nullable = false)
    private Integer ordem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ponto", nullable = false, length = 20)
    private TipoPonto tipoPonto;

    @Column(name = "horario_estimado")
    private LocalDateTime horarioEstimado;

    protected PontoRota() {
    }

    public PontoRota(Rota rota, Cidade cidade, LocalAdministrativo local, Integer ordem, TipoPonto tipoPonto, LocalDateTime horarioEstimado) {
        this.rota = rota;
        this.cidade = cidade;
        this.local = local;
        this.ordem = ordem;
        this.tipoPonto = tipoPonto;
        this.horarioEstimado = horarioEstimado;
    }

    public Long getId() {
        return id;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public LocalAdministrativo getLocal() {
        return local;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public TipoPonto getTipoPonto() {
        return tipoPonto;
    }

    public LocalDateTime getHorarioEstimado() {
        return horarioEstimado;
    }
}
