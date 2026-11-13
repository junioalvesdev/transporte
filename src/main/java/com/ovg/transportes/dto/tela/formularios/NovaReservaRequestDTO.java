package com.ovg.transportes.dto.tela.formularios;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * O que o formulario "Criar Reservas" envia (pelo name de cada campo).
 * Origem/destino: ou uma unidade (localOrigemId), ou "outro local" com a
 * cidade escolhida na lista ("Goiania/GO") e uma descricao opcional.
 */
public record NovaReservaRequestDTO(
    String finalidade,
    Long localOrigemId,
    String cidadeOrigem,
    String descricaoOrigem,
    Long localDestinoId,
    String cidadeDestino,
    String descricaoDestino,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime saida,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime retorno,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> replicarDias,
    Integer qtdPassageiros,
    String telefone,
    String observacoes
) {
}
