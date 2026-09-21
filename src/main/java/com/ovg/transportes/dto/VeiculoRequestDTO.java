package com.ovg.transportes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VeiculoRequestDTO(
    @NotBlank String placa,
    @NotBlank String modelo,
    @NotNull @Min(1) Integer capacidade,
    @NotNull Long tipoVeiculoId,
    Long localId
) {
}
