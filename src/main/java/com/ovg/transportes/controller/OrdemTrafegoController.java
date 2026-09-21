package com.ovg.transportes.controller;

import com.ovg.transportes.dto.OrdemTrafegoRequestDTO;
import com.ovg.transportes.service.OrdemTrafegoService;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordens-trafego")
public class OrdemTrafegoController {

    private final OrdemTrafegoService ordemTrafegoService;

    public OrdemTrafegoController(OrdemTrafegoService ordemTrafegoService) {
        this.ordemTrafegoService = ordemTrafegoService;
    }

    @PostMapping("/pdf")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public ResponseEntity<byte[]> gerarPdf(@RequestBody @Valid OrdemTrafegoRequestDTO requisicao) {
        byte[] pdf = ordemTrafegoService.gerarPdf(requisicao);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("ordem-de-trafego.pdf").build().toString())
            .body(pdf);
    }
}
