package com.ovg.transportes.dto.tela.calendario;

import com.ovg.transportes.model.StatusViagem;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * O que o calendario mostra, vindo da URL:
 * /calendario?modo=semana&data=2026-09-22&status=PLANEJADA&busca=2026-09-25&passageiros=2
 */
public record FiltrosCalendarioDTO(
    String modo,                                                    // "mes" (padrao) ou "semana"
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,  // dia de referencia (padrao: hoje)
    List<StatusViagem> status,                                      // status de viagem visiveis (vazio = todos)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate busca, // busca de carona: dia (abre o modal de resultados)
    Integer passageiros                                             // busca de carona: quantos lugares
) {
}
