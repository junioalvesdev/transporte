package com.ovg.transportes.dto.tela.calendario;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * O que o formulario de editar uma solicitacao pendente (quem pediu) envia.
 * Origem/destino como no "Criar Reservas": unidade OU cidade da lista + descricao.
 */
public record EditarSolicitacaoRequestDTO(
    String finalidade,
    Long localOrigemId,
    String cidadeOrigem,
    String descricaoOrigem,
    Long localDestinoId,
    String cidadeDestino,
    String descricaoDestino,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime saida,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime retorno,
    Integer qtdPassageiros,
    String telefone,
    String observacoes
) {
}
