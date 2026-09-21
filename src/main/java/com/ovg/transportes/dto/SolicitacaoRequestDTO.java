package com.ovg.transportes.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Origem e destino aceitam DUAS formas, mutuamente exclusivas — o service
 * decide qual foi usada e valida que so uma delas veio preenchida:
 * 1) localOrigemId/localDestinoId: o usuario escolheu uma unidade administrativa
 *    (SEDE, CISF...) — caso mais comum, resolve a cidade "de casa" da unidade.
 * 2) cidadeOrigemId/cidadeDestinoId (+ descricaoOrigem/descricaoDestino
 *    opcional): o usuario escolheu "outro local" e procurou uma cidade,
 *    com um texto livre pra dar precisao (endereco, ponto de referencia).
 */
public record SolicitacaoRequestDTO(
    Long localOrigemId,
    Long cidadeOrigemId,
    String descricaoOrigem,
    Long localDestinoId,
    Long cidadeDestinoId,
    String descricaoDestino,
    @NotNull @Future LocalDateTime dataHoraDesejada,
    @NotNull @Future LocalDateTime dataHoraRetornoDesejada,
    @NotNull @Min(1) Integer qtdPassageiros,
    @NotBlank String telefoneContato,
    @NotBlank String finalidade,
    String observacoes
) {
}
