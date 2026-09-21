package com.ovg.transportes.controller;

import com.ovg.transportes.dto.AtualizarViagemRequestDTO;
import com.ovg.transportes.dto.BuscaViagemRequestDTO;
import com.ovg.transportes.dto.CriarViagemRequestDTO;
import com.ovg.transportes.dto.SolicitarVagaRequestDTO;
import com.ovg.transportes.dto.ViagemDisponivelDTO;
import com.ovg.transportes.dto.ViagemResponseDTO;
import com.ovg.transportes.model.StatusViagem;
import com.ovg.transportes.service.ViagemDisponivelService;
import com.ovg.transportes.service.ViagemService;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/viagens")
public class ViagemController {

    private final ViagemDisponivelService viagemDisponivelService;
    private final ViagemService viagemService;

    public ViagemController(ViagemDisponivelService viagemDisponivelService, ViagemService viagemService) {
        this.viagemDisponivelService = viagemDisponivelService;
        this.viagemService = viagemService;
    }

    @GetMapping("/disponiveis")
    public List<ViagemDisponivelDTO> buscarDisponiveis(@Valid BuscaViagemRequestDTO requisicao) {
        return viagemDisponivelService.buscar(requisicao);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TRANSPORTE')")
    public ViagemResponseDTO criar(@RequestBody @Valid CriarViagemRequestDTO requisicao) {
        return viagemService.criarViagemAutonoma(requisicao);
    }

    @PostMapping("/{id}/participantes")
    @ResponseStatus(HttpStatus.CREATED)
    public ViagemResponseDTO solicitarVaga(@PathVariable Long id, @RequestBody @Valid SolicitarVagaRequestDTO requisicao) {
        return viagemService.solicitarVaga(id, requisicao);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public ViagemResponseDTO transicionarStatus(@PathVariable Long id, @RequestParam StatusViagem novoStatus) {
        return viagemService.transicionarStatus(id, novoStatus);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public ViagemResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid AtualizarViagemRequestDTO requisicao) {
        return viagemService.atualizarDetalhes(id, requisicao);
    }

    // qualquer usuario autenticado pode VER o calendario (so nao pode editar —
    // os PATCH/POST acima exigem TRANSPORTE); a tela decide o que mostrar de
    // acao conforme o perfil (ver GET /api/auth/me)
    @GetMapping("/calendario")
    public List<ViagemResponseDTO> calendario(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        return viagemService.listarNoPeriodo(inicio, fim);
    }
}
