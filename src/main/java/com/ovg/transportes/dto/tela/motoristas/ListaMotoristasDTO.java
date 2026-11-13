package com.ovg.transportes.dto.tela.motoristas;

import com.ovg.transportes.dto.tela.layout.PaginacaoDTO;

import java.util.List;

/** Tudo que a pagina de motoristas mostra (vai pro HTML como ${pagina}). */
public record ListaMotoristasDTO(
    List<LinhaMotoristaDTO> linhas,
    PaginacaoDTO paginacao,
    int paginaAtual             // pra voltar pra mesma pagina depois de salvar
) {
}
