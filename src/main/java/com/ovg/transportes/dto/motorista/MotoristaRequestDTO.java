package com.ovg.transportes.dto.motorista;

import jakarta.validation.constraints.NotBlank;

public record MotoristaRequestDTO(
    @NotBlank String nome,
    String telefone,
    Long localId
) {
}
