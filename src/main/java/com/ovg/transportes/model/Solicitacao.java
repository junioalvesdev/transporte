package com.ovg.transportes.model;

import com.ovg.transportes.common.Auditavel;
import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.model.Cidade;
import com.ovg.transportes.model.LocalAdministrativo;
import com.ovg.transportes.model.Usuario;

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
@Table(name = "solicitacao")
public class Solicitacao extends Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    // backbone estruturado que o motor de compatibilidade de rota entende —
    // sempre preenchido, mesmo quando o usuario so escolheu uma unidade (nesse
    // caso e a cidade "de casa" da unidade, ver LocalAdministrativo.cidade).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidade_origem_id", nullable = false)
    private Cidade cidadeOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidade_destino_id", nullable = false)
    private Cidade cidadeDestino;

    // preenchido quando o usuario escolheu uma unidade administrativa em vez de
    // procurar uma cidade — e o caminho mais comum (a maioria das corridas e
    // local, nao intermunicipal).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_origem_id")
    private LocalAdministrativo localOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_destino_id")
    private LocalAdministrativo localDestino;

    // preenchido so quando o usuario escolheu "outro local" (cidade) em vez de
    // uma unidade — da a precisao que uma cidade sozinha nao da (endereco,
    // ponto de referencia).
    @Column(name = "descricao_origem", length = 400)
    private String descricaoOrigem;

    @Column(name = "descricao_destino", length = 400)
    private String descricaoDestino;

    @Column(name = "data_hora_desejada", nullable = false)
    private LocalDateTime dataHoraDesejada;

    // opcional: nem toda solicitacao e ida e volta.
    @Column(name = "data_hora_retorno_desejada")
    private LocalDateTime dataHoraRetornoDesejada;

    @Column(name = "qtd_passageiros", nullable = false)
    private Integer qtdPassageiros;

    @Column(name = "telefone_contato", length = 20)
    private String telefoneContato;

    @Column(length = 200)
    private String finalidade;

    @Column(length = 500)
    private String observacoes;

    @Column(nullable = false)
    private boolean urgente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    protected Solicitacao() {
    }

    public Solicitacao(
        Usuario solicitante,
        LocalAdministrativo localOrigem,
        Cidade cidadeOrigem,
        String descricaoOrigem,
        LocalAdministrativo localDestino,
        Cidade cidadeDestino,
        String descricaoDestino,
        LocalDateTime dataHoraDesejada,
        LocalDateTime dataHoraRetornoDesejada,
        Integer qtdPassageiros,
        String telefoneContato,
        String finalidade,
        String observacoes,
        boolean urgente
    ) {
        this.solicitante = solicitante;
        this.localOrigem = localOrigem;
        this.cidadeOrigem = cidadeOrigem;
        this.descricaoOrigem = descricaoOrigem;
        this.localDestino = localDestino;
        this.cidadeDestino = cidadeDestino;
        this.descricaoDestino = descricaoDestino;
        this.dataHoraDesejada = dataHoraDesejada;
        this.dataHoraRetornoDesejada = dataHoraRetornoDesejada;
        this.qtdPassageiros = qtdPassageiros;
        this.telefoneContato = telefoneContato;
        this.finalidade = finalidade;
        this.observacoes = observacoes;
        this.urgente = urgente;
    }

    public void aprovar() {
        transicionarPara(StatusSolicitacao.APROVADA);
    }

    public void reprovar(String motivo) {
        this.motivoRecusa = motivo;
        transicionarPara(StatusSolicitacao.REPROVADA);
    }

    public void marcarComoAtendida() {
        transicionarPara(StatusSolicitacao.ATENDIDA);
    }

    public void cancelar() {
        if (dataHoraDesejada.isBefore(LocalDateTime.now())) {
            throw new NegocioException("Nao e possivel cancelar uma solicitacao cuja viagem ja deveria ter saido");
        }
        transicionarPara(StatusSolicitacao.CANCELADA);
    }

    private void transicionarPara(StatusSolicitacao novoStatus) {
        if (!status.podeTransicionarPara(novoStatus)) {
            throw new NegocioException("Transicao invalida: %s -> %s".formatted(status, novoStatus));
        }
        this.status = novoStatus;
    }

    public Long getId() {
        return id;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public Cidade getCidadeOrigem() {
        return cidadeOrigem;
    }

    public Cidade getCidadeDestino() {
        return cidadeDestino;
    }

    public LocalAdministrativo getLocalOrigem() {
        return localOrigem;
    }

    public LocalAdministrativo getLocalDestino() {
        return localDestino;
    }

    public String getDescricaoOrigem() {
        return descricaoOrigem;
    }

    public String getDescricaoDestino() {
        return descricaoDestino;
    }

    public LocalDateTime getDataHoraDesejada() {
        return dataHoraDesejada;
    }

    public LocalDateTime getDataHoraRetornoDesejada() {
        return dataHoraRetornoDesejada;
    }

    public Integer getQtdPassageiros() {
        return qtdPassageiros;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public String getFinalidade() {
        return finalidade;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public boolean isUrgente() {
        return urgente;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public String getMotivoRecusa() {
        return motivoRecusa;
    }
}
