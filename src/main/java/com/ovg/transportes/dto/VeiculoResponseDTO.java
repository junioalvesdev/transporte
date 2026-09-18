package com.ovg.transportes.dto;

import com.ovg.transportes.model.Veiculo;

public record VeiculoResponseDTO(
    Long id,
    String placa,
    String modelo,
    Integer capacidade,
    String tipoVeiculo,
    String local,
    boolean ativo
) {

    public static VeiculoResponseDTO de(Veiculo veiculo) {
        return new VeiculoResponseDTO(
            veiculo.getId(),
            veiculo.getPlaca(),
            veiculo.getModelo(),
            veiculo.getCapacidade(),
            veiculo.getTipoVeiculo().getNome(),
            veiculo.getLocal() != null ? veiculo.getLocal().getNome() : null,
            veiculo.isAtivo()
        );
    }
}
