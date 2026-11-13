package com.ovg.transportes.dto.tela.calendario;

import com.ovg.transportes.model.StatusViagem;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** O que o formulario de editar viagem (modal de detalhe do calendario) envia. */
public record EditarViagemRequestDTO(
    Long veiculoId,
    Long motoristaId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime saida,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime retorno,
    Integer capacidade,
    StatusViagem status,
    StatusViagem statusAtual    // campo escondido: o status antes de editar (so troca se mudou)
) {
}
