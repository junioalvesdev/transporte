package com.ovg.transportes.dto;

import com.ovg.transportes.model.TipoPonto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PontoRotaRequestDTO(
    Long localId,
    @NotNull Long cidadeId,
    @NotNull Integer ordem,
    @NotNull TipoPonto tipoPonto,
    LocalDateTime horarioEstimado
) {
}
