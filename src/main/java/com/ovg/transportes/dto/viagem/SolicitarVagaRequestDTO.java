package com.ovg.transportes.dto.viagem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SolicitarVagaRequestDTO(
    @NotNull @Min(1) Integer qtdPassageiros,
    @NotNull Long cidadeEmbarqueId,
    @NotNull Long cidadeDesembarqueId,
    String finalidade,
    String observacoes
) {
}
