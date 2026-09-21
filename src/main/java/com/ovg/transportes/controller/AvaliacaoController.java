package com.ovg.transportes.controller;

import com.ovg.transportes.dto.AvaliacaoRequestDTO;
import com.ovg.transportes.service.AvaliacaoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/viagens/participantes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping("/{id}/avaliacao")
    @ResponseStatus(HttpStatus.CREATED)
    public void avaliar(@PathVariable Long id, @RequestBody @Valid AvaliacaoRequestDTO requisicao) {
        avaliacaoService.avaliar(id, requisicao);
    }
}
