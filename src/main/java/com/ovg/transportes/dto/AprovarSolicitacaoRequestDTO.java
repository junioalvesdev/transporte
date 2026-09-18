package com.ovg.transportes.dto;

import com.ovg.transportes.dto.CriarViagemRequestDTO;

import jakarta.validation.constraints.NotNull;

/**
 * Ou informa {@code viagemExistenteId} (vincula a uma viagem ja aberta para
 * aproveitamento), ou {@code novaViagem} (cria uma viagem nova para atender
 * esta solicitacao) — nunca os dois.
 */
public record AprovarSolicitacaoRequestDTO(
    Long viagemExistenteId,
    CriarViagemRequestDTO novaViagem,
    @NotNull Long cidadeEmbarqueId,
    @NotNull Long cidadeDesembarqueId
) {
}
