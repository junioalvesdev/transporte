package com.ovg.transportes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AtualizarViagemRequestDTO(
    @NotNull Long veiculoId,
    @NotNull Long motoristaId,
    @NotNull LocalDateTime dataHoraSaida,
    LocalDateTime dataHoraChegadaEstimada,
    @NotNull @Min(1) Integer capacidadeTotal
) {
}
