package com.ovg.transportes.controller.api;

import com.ovg.transportes.dto.local.CidadeResponseDTO;
import com.ovg.transportes.service.CidadeService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cidades")
public class CidadeController {

    private final CidadeService cidadeService;

    public CidadeController(CidadeService cidadeService) {
        this.cidadeService = cidadeService;
    }

    @GetMapping
    public List<CidadeResponseDTO> buscar(@RequestParam String nome) {
        return cidadeService.buscarPorNome(nome);
    }
}
