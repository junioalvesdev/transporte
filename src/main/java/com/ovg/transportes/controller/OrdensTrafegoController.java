package com.ovg.transportes.controller;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.ordemtrafego.OrdemTrafegoRequestDTO;
import com.ovg.transportes.service.tela.OrdensTrafegoService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Endpoint do modal "Gerar Ordem de Trafego" (templates/fragmentos/modal-ordem-trafego.html),
 * usado na Lista de Reservas e no Calendario. O form abre numa aba nova
 * (target="_blank") e recebe o PDF direto.
 */
@Controller
@PreAuthorize("hasRole('TRANSPORTE')")
public class OrdensTrafegoController {

    private final OrdensTrafegoService ordensTrafegoService;

    public OrdensTrafegoController(OrdensTrafegoService ordensTrafegoService) {
        this.ordensTrafegoService = ordensTrafegoService;
    }

    @PostMapping("/ordens-trafego")
    public ResponseEntity<byte[]> gerar(
        @RequestParam(required = false) Long veiculoId,
        @RequestParam(required = false) Long motoristaId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        @RequestParam(defaultValue = "") String unidade
    ) {
        try {
            byte[] pdf = ordensTrafegoService.gerarPdf(new OrdemTrafegoRequestDTO(veiculoId, motoristaId, data, dataFim, unidade));
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("ordem-de-trafego.pdf").build().toString())
                .body(pdf);
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            return ResponseEntity.unprocessableEntity()
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(("Nao foi possivel gerar a ordem de trafego: " + erro.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }
}
