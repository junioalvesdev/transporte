package com.ovg.transportes.dto.tela.minhas;

import com.ovg.transportes.dto.tela.layout.PaginacaoDTO;

import java.util.List;

/** Tudo que a pagina "Minhas solicitacoes" mostra (vai pro HTML como ${pagina}). */
public record ListaMinhasSolicitacoesDTO(
    List<CartaoMinhaSolicitacaoDTO> cartoes,
    PaginacaoDTO paginacao,
    int paginaAtual             // pra voltar pra mesma pagina depois de cancelar
) {
}
