package com.ovg.transportes.controller;

import com.ovg.transportes.dto.AprovarSolicitacaoRequestDTO;
import com.ovg.transportes.dto.AtualizarSolicitacaoRequestDTO;
import com.ovg.transportes.dto.PaginaDTO;
import com.ovg.transportes.dto.ReprovarSolicitacaoRequestDTO;
import com.ovg.transportes.dto.SolicitacaoRequestDTO;
import com.ovg.transportes.dto.SolicitacaoResponseDTO;
import com.ovg.transportes.dto.ViagemResponseDTO;
import com.ovg.transportes.model.StatusSolicitacao;
import com.ovg.transportes.service.SolicitacaoService;
import com.ovg.transportes.service.ViagemService;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;
    private final ViagemService viagemService;

    public SolicitacaoController(SolicitacaoService solicitacaoService, ViagemService viagemService) {
        this.solicitacaoService = solicitacaoService;
        this.viagemService = viagemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitacaoResponseDTO criar(@RequestBody @Valid SolicitacaoRequestDTO requisicao) {
        return solicitacaoService.criar(requisicao);
    }

    @GetMapping("/minhas")
    public PaginaDTO<SolicitacaoResponseDTO> listarMinhas(
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamanho
    ) {
        return solicitacaoService.listarMinhas(PageRequest.of(pagina, tamanho));
    }

    @GetMapping
    @PreAuthorize("hasRole('TRANSPORTE')")
    public PaginaDTO<SolicitacaoResponseDTO> listarPorStatus(
        @RequestParam(defaultValue = "PENDENTE") StatusSolicitacao status,
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamanho
    ) {
        return solicitacaoService.listarPorStatus(status, PageRequest.of(pagina, tamanho));
    }

    // "Lista de Reservas": todas as solicitacoes juntas, de qualquer status —
    // pagina separada de "Solicitacoes pendentes" (que so lista PENDENTE)
    @GetMapping("/todas")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public PaginaDTO<SolicitacaoResponseDTO> listarTodas(
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "1000") int tamanho
    ) {
        return solicitacaoService.listarTodas(PageRequest.of(pagina, tamanho));
    }

    // Calendario: solicitacoes que ainda nao viraram viagem (pendente,
    // reprovada, cancelada) no periodo visivel — qualquer usuario autenticado
    // pode chamar, mas o service so devolve as dele mesmo quando nao e TRANSPORTE
    @PatchMapping("/{id}")
    public SolicitacaoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid AtualizarSolicitacaoRequestDTO requisicao) {
        return solicitacaoService.atualizar(id, requisicao);
    }

    @GetMapping("/calendario")
    public List<SolicitacaoResponseDTO> calendario(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        return solicitacaoService.listarNoPeriodoParaCalendario(inicio, fim);
    }

    @PatchMapping("/{id}/cancelar")
    public SolicitacaoResponseDTO cancelar(@PathVariable Long id) {
        return solicitacaoService.cancelar(id);
    }

    @PatchMapping("/{id}/reprovar")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public SolicitacaoResponseDTO reprovar(@PathVariable Long id, @RequestBody @Valid ReprovarSolicitacaoRequestDTO requisicao) {
        return solicitacaoService.reprovar(id, requisicao);
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public ViagemResponseDTO aprovar(@PathVariable Long id, @RequestBody @Valid AprovarSolicitacaoRequestDTO requisicao) {
        return viagemService.aprovarSolicitacao(id, requisicao);
    }
}
