package com.ovg.transportes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OrdemTrafegoRequestDTO(
    @NotNull Long veiculoId,
    @NotNull Long motoristaId,
    @NotNull LocalDate data,
    LocalDate dataFim,
    @NotBlank String unidade
) {
}
