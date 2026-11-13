package com.ovg.transportes.dto.tela.veiculos;

import com.ovg.transportes.common.tela.Opcao;
import com.ovg.transportes.dto.tela.layout.PaginacaoDTO;

import java.util.List;

/** Tudo que a pagina de veiculos mostra (vai pro HTML como ${pagina}). */
public record ListaVeiculosDTO(
    List<LinhaVeiculoDTO> linhas,
    PaginacaoDTO paginacao,
    List<Opcao> tipos,          // select de tipo do modal "Novo veiculo"
    int paginaAtual             // pra voltar pra mesma pagina depois de salvar
) {
}
