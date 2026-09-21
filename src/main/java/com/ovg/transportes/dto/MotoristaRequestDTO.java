package com.ovg.transportes.dto;

import jakarta.validation.constraints.NotBlank;

public record MotoristaRequestDTO(
    @NotBlank String nome,
    String telefone,
    Long localId
) {
}
