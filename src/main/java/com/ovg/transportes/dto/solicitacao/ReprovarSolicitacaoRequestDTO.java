package com.ovg.transportes.dto.solicitacao;

import jakarta.validation.constraints.NotBlank;

public record ReprovarSolicitacaoRequestDTO(
    @NotBlank String motivoRecusa
) {
}
