package com.ovg.transportes.controller;

import com.ovg.transportes.service.tela.RelatoriosService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** Endpoint da pagina "Relatorios" (templates/relatorios/relatorio.html). */
@Controller
@RequestMapping("/relatorios")
@PreAuthorize("hasRole('TRANSPORTE')")
public class RelatoriosController {

    private final RelatoriosService relatoriosService;

    public RelatoriosController(RelatoriosService relatoriosService) {
        this.relatoriosService = relatoriosService;
    }

    // GET /relatorios?inicio=2025-09-01&fim=2026-09-30 (sem datas: o ultimo ano)
    @GetMapping
    public String gerar(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
        Model model
    ) {
        model.addAttribute("pagina", relatoriosService.montar(inicio, fim));
        return "relatorios/relatorio";
    }
}
