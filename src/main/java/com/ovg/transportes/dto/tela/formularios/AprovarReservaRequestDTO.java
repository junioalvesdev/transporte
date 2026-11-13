package com.ovg.transportes.dto.tela.formularios;

import com.ovg.transportes.model.StatusViagem;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** O que o formulario de aprovar (no detalhe da reserva) envia. */
public record AprovarReservaRequestDTO(
    Long veiculoId,
    Long motoristaId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime saida,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime retorno,
    StatusViagem statusViagem
) {
}
