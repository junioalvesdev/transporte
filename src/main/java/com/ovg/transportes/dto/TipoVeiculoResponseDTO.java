package com.ovg.transportes.dto;

import com.ovg.transportes.model.TipoVeiculo;

public record TipoVeiculoResponseDTO(Long id, String nome) {

    public static TipoVeiculoResponseDTO de(TipoVeiculo tipoVeiculo) {
        return new TipoVeiculoResponseDTO(tipoVeiculo.getId(), tipoVeiculo.getNome());
    }
}
