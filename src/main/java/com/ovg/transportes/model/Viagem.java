package com.ovg.transportes.model;

import com.ovg.transportes.common.Auditavel;
import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.model.Motorista;
import com.ovg.transportes.model.Rota;
import com.ovg.transportes.model.Veiculo;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "viagem")
public class Viagem extends Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id", nullable = false)
    private Motorista motorista;

    @Column(name = "data_hora_saida", nullable = false)
    private LocalDateTime dataHoraSaida;

    @Column(name = "data_hora_chegada_estimada")
    private LocalDateTime dataHoraChegadaEstimada;

    @Column(name = "capacidade_total", nullable = false)
    private Integer capacidadeTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusViagem status = StatusViagem.PLANEJADA;

    @OneToMany(mappedBy = "viagem", fetch = FetchType.LAZY)
    private List<ViagemParticipante> participantes = new ArrayList<>();

    @OneToOne(mappedBy = "viagem", fetch = FetchType.LAZY)
    private Rota rota;

    protected Viagem() {
    }

    public Viagem(Veiculo veiculo, Motorista motorista, LocalDateTime dataHoraSaida, LocalDateTime dataHoraChegadaEstimada) {
        this.veiculo = veiculo;
        this.motorista = motorista;
        this.dataHoraSaida = dataHoraSaida;
        this.dataHoraChegadaEstimada = dataHoraChegadaEstimada;
        // snapshot da capacidade no momento da criacao: se o veiculo for editado depois,
        // uma viagem ja criada nao muda de capacidade "magicamente" por baixo dos panos.
        this.capacidadeTotal = veiculo.getCapacidade();
    }

    public int vagasOcupadas() {
        return participantes.stream()
            .filter(participante -> participante.getStatus() != StatusParticipante.CANCELADO)
            .mapToInt(ViagemParticipante::getQtdPassageiros)
            .sum();
    }

    public int vagasDisponiveis() {
        return capacidadeTotal - vagasOcupadas();
    }

    public boolean temVagaPara(int qtdPassageirosSolicitados) {
        return vagasDisponiveis() >= qtdPassageirosSolicitados;
    }

    /**
     * Reatribui veiculo/motorista/horarios/capacidade de uma viagem ja criada —
     * usado pelo Departamento de Transportes no calendario para corrigir dados
     * (ex.: trocou o veiculo no ultimo minuto). Viagens cuja saida ja passou
     * ficam congeladas: nada muda mais depois que a viagem ja deveria ter partido.
     */
    public void atualizarDetalhes(Veiculo veiculo, Motorista motorista, LocalDateTime dataHoraSaida, LocalDateTime dataHoraChegadaEstimada, Integer capacidadeTotal) {
        if (this.dataHoraSaida.toLocalDate().isBefore(LocalDate.now())) {
            throw new NegocioException("Nao e possivel alterar uma viagem cuja data de saida ja passou");
        }
        if (capacidadeTotal < vagasOcupadas()) {
            throw new NegocioException("A nova capacidade (%d) e menor que a ocupacao atual (%d)".formatted(capacidadeTotal, vagasOcupadas()));
        }
        this.veiculo = veiculo;
        this.motorista = motorista;
        this.dataHoraSaida = dataHoraSaida;
        this.dataHoraChegadaEstimada = dataHoraChegadaEstimada;
        this.capacidadeTotal = capacidadeTotal;
    }

    public void transicionarPara(StatusViagem novoStatus) {
        if (!status.podeTransicionarPara(novoStatus)) {
            throw new NegocioException("Transicao invalida: %s -> %s".formatted(status, novoStatus));
        }
        this.status = novoStatus;
    }

    /**
     * Reavalia o status apos um participante entrar ou sair, refletindo a ocupacao
     * real — corrige o legado, onde a disponibilidade do veiculo era travada/liberada
     * manualmente e nunca em funcao da ocupacao de fato.
     */
    public void recalcularStatusPorOcupacao() {
        if (status != StatusViagem.ABERTA_PARA_APROVEITAMENTO && status != StatusViagem.LOTADA) {
            return;
        }
        StatusViagem statusRecalculado = vagasDisponiveis() == 0 ? StatusViagem.LOTADA : StatusViagem.ABERTA_PARA_APROVEITAMENTO;
        if (statusRecalculado != status) {
            transicionarPara(statusRecalculado);
        }
    }

    public Long getId() {
        return id;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public Motorista getMotorista() {
        return motorista;
    }

    public LocalDateTime getDataHoraSaida() {
        return dataHoraSaida;
    }

    public LocalDateTime getDataHoraChegadaEstimada() {
        return dataHoraChegadaEstimada;
    }

    public Integer getCapacidadeTotal() {
        return capacidadeTotal;
    }

    public StatusViagem getStatus() {
        return status;
    }

    public List<ViagemParticipante> getParticipantes() {
        return participantes;
    }

    public Rota getRota() {
        return rota;
    }
}
