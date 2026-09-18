package com.ovg.transportes.dto;

import com.ovg.transportes.model.LocalAdministrativo;

public record LocalAdministrativoResponseDTO(
    Long id,
    String nome,
    Long cidadeId,
    String cidadeNomeComUf
) {

    public static LocalAdministrativoResponseDTO de(LocalAdministrativo local) {
        return new LocalAdministrativoResponseDTO(
            local.getId(),
            local.getNome(),
            local.getCidade().getId(),
            local.getCidade().nomeComUf()
        );
    }
}
