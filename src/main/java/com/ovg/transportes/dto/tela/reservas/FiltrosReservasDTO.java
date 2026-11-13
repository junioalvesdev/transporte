package com.ovg.transportes.dto.tela.reservas;

import com.ovg.transportes.model.StatusSolicitacao;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * Filtros da Lista de Reservas, vindos da URL (?status=...&de=...&ate=...).
 * O Spring preenche sozinho a partir dos campos do formulario de filtros.
 */
public record FiltrosReservasDTO(
    List<StatusSolicitacao> status,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate
) {
}
