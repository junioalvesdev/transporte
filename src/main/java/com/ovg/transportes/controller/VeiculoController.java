package com.ovg.transportes.controller;

import com.ovg.transportes.dto.PaginaDTO;
import com.ovg.transportes.dto.VeiculoRequestDTO;
import com.ovg.transportes.dto.VeiculoResponseDTO;
import com.ovg.transportes.service.VeiculoService;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping
    public PaginaDTO<VeiculoResponseDTO> listar(
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamanho
    ) {
        return veiculoService.listarAtivos(PageRequest.of(pagina, tamanho));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TRANSPORTE')")
    public VeiculoResponseDTO cadastrar(@RequestBody @Valid VeiculoRequestDTO requisicao) {
        return veiculoService.cadastrar(requisicao);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public VeiculoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid VeiculoRequestDTO requisicao) {
        return veiculoService.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TRANSPORTE')")
    public void inativar(@PathVariable Long id) {
        veiculoService.inativar(id);
    }
}
