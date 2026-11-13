package com.ovg.transportes.dto.tela.calendario;

import java.util.List;

/**
 * Busca de carona do cabecalho do calendario. Quando a URL traz ?busca=dia,
 * o modal de resultados ja volta aberto (sem JS), com os cartoes prontos.
 */
public record BuscaCaronaDTO(
    boolean aberta,
    String classeModal,         // "modal fade" ou "modal fade show d-block" (aberto)
    String data,                // valor do campo de data (yyyy-MM-dd)
    String dataMinima,          // hoje: nao da pra buscar no passado
    int passageiros,
    String titulo,              // "Viagens disponiveis em 24/09/2026"
    String erro,                // busca invalida, ou null
    List<CartaoCaronaDTO> cartoes,
    boolean semResultados,      // busca valida, mas nenhuma viagem aberta no dia
    String urlFechar            // o calendario sem a busca
) {
}
