package com.ovg.transportes.model;

import com.ovg.transportes.model.Cidade;
import com.ovg.transportes.model.Solicitacao;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "viagem_participante")
public class ViagemParticipante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", nullable = false)
    private Viagem viagem;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private Solicitacao solicitacao;

    @Column(name = "qtd_passageiros", nullable = false)
    private Integer qtdPassageiros;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidade_embarque_id", nullable = false)
    private Cidade cidadeEmbarque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidade_desembarque_id", nullable = false)
    private Cidade cidadeDesembarque;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusParticipante status = StatusParticipante.CONFIRMADO;

    protected ViagemParticipante() {
    }

    public ViagemParticipante(
        Viagem viagem,
        Solicitacao solicitacao,
        Integer qtdPassageiros,
        Cidade cidadeEmbarque,
        Cidade cidadeDesembarque
    ) {
        this.viagem = viagem;
        this.solicitacao = solicitacao;
        this.qtdPassageiros = qtdPassageiros;
        this.cidadeEmbarque = cidadeEmbarque;
        this.cidadeDesembarque = cidadeDesembarque;
    }

    public void cancelar() {
        this.status = StatusParticipante.CANCELADO;
    }

    public Long getId() {
        return id;
    }

    public Viagem getViagem() {
        return viagem;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public Integer getQtdPassageiros() {
        return qtdPassageiros;
    }

    public Cidade getCidadeEmbarque() {
        return cidadeEmbarque;
    }

    public Cidade getCidadeDesembarque() {
        return cidadeDesembarque;
    }

    public StatusParticipante getStatus() {
        return status;
    }
}
