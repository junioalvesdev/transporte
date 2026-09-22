package com.ovg.transportes.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Mesma forma de SolicitacaoRequestDTO, mas pra editar uma solicitacao ja
 * existente — dataHoraRetornoDesejada NAO e obrigatoria aqui (diferente de
 * criar uma nova pela tela, que sempre pede as duas datas): varias
 * solicitacoes migradas do sistema legado sao so de ida, sem retorno
 * registrado, e precisam continuar editaveis mesmo assim.
 */
public record AtualizarSolicitacaoRequestDTO(
    Long localOrigemId,
    Long cidadeOrigemId,
    String descricaoOrigem,
    Long localDestinoId,
    Long cidadeDestinoId,
    String descricaoDestino,
    @NotNull @Future LocalDateTime dataHoraDesejada,
    LocalDateTime dataHoraRetornoDesejada,
    @NotNull @Min(1) Integer qtdPassageiros,
    @NotBlank String telefoneContato,
    @NotBlank String finalidade,
    String observacoes
) {
}
