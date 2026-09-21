package com.ovg.transportes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoRequestDTO(
    @NotNull @Min(1) @Max(5) Integer nota,
    String categoria,
    String comentario
) {
}
