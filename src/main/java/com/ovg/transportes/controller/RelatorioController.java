package com.ovg.transportes.controller;

import com.ovg.transportes.dto.RelatorioMensalDTO;
import com.ovg.transportes.service.RelatorioService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/mensal")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public RelatorioMensalDTO relatorioMensal(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fim
    ) {
        return relatorioService.gerar(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
    }
}
