package com.ovg.transportes.dto;

import com.ovg.transportes.model.StatusParticipante;
import com.ovg.transportes.model.StatusViagem;
import com.ovg.transportes.model.Viagem;

import java.time.LocalDateTime;
import java.util.List;

public record ViagemResponseDTO(
    Long id,
    Long veiculoId,
    String veiculoPlaca,
    Long motoristaId,
    String motoristaNome,
    LocalDateTime dataHoraSaida,
    LocalDateTime dataHoraChegadaEstimada,
    Integer capacidadeTotal,
    int vagasOcupadas,
    StatusViagem status,
    List<String> solicitantesNomes,
    String unidadeOrigem
) {

    public static ViagemResponseDTO de(Viagem viagem) {
        List<String> solicitantesNomes = viagem.getParticipantes().stream()
            .filter(participante -> participante.getStatus() != StatusParticipante.CANCELADO)
            .map(participante -> participante.getSolicitacao().getSolicitante().getNome())
            .distinct()
            .toList();

        // usado pra pre-preencher a "Unidade" na Ordem de Trafego quando a rota veio de uma unidade da OVG
        String unidadeOrigem = viagem.getRota() != null && !viagem.getRota().getPontos().isEmpty()
            && viagem.getRota().getPontos().get(0).getLocal() != null
            ? viagem.getRota().getPontos().get(0).getLocal().getNome()
            : null;

        return new ViagemResponseDTO(
            viagem.getId(),
            viagem.getVeiculo().getId(),
            viagem.getVeiculo().getPlaca(),
            viagem.getMotorista().getId(),
            viagem.getMotorista().getNome(),
            viagem.getDataHoraSaida(),
            viagem.getDataHoraChegadaEstimada(),
            viagem.getCapacidadeTotal(),
            viagem.vagasOcupadas(),
            viagem.getStatus(),
            solicitantesNomes,
            unidadeOrigem
        );
    }
}
