package com.ovg.transportes.dto;

import com.ovg.transportes.model.Cidade;

public record CidadeResponseDTO(Long id, String nome, String uf) {

    public static CidadeResponseDTO de(Cidade cidade) {
        return new CidadeResponseDTO(cidade.getId(), cidade.getNome(), cidade.getEstado().getUf());
    }
}
