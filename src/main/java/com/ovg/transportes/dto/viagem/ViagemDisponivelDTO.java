package com.ovg.transportes.dto.viagem;

import java.time.LocalDateTime;

public record ViagemDisponivelDTO(
    Long viagemId,
    Long cidadeOrigemId,
    String origem,
    Long cidadeDestinoId,
    String destino,
    LocalDateTime dataHoraSaida,
    LocalDateTime dataHoraChegadaEstimada,
    String tipoVeiculo,
    int vagasDisponiveis,
    Long minhaSolicitacaoId
) {
}
