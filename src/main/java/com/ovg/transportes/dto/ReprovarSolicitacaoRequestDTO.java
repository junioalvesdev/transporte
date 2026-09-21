package com.ovg.transportes.dto;

import jakarta.validation.constraints.NotBlank;

public record ReprovarSolicitacaoRequestDTO(
    @NotBlank String motivoRecusa
) {
}
