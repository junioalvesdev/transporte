package com.ovg.transportes.dto;

import com.ovg.transportes.model.Motorista;

public record MotoristaResponseDTO(
    Long id,
    String nome,
    String telefone,
    String local,
    boolean ativo
) {

    public static MotoristaResponseDTO de(Motorista motorista) {
        return new MotoristaResponseDTO(
            motorista.getId(),
            motorista.getNome(),
            motorista.getTelefone(),
            motorista.getLocal() != null ? motorista.getLocal().getNome() : null,
            motorista.isAtivo()
        );
    }
}
