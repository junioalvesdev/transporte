package com.ovg.transportes.dto;

import com.ovg.transportes.dto.PontoRotaRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record CriarViagemRequestDTO(
    @NotNull Long veiculoId,
    @NotNull Long motoristaId,
    @NotNull @Future LocalDateTime dataHoraSaida,
    @NotNull @Future LocalDateTime dataHoraChegadaEstimada,
    @NotEmpty @Valid List<PontoRotaRequestDTO> pontos
) {
}
