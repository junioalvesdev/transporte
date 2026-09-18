package com.ovg.transportes.controller;

import com.ovg.transportes.dto.MotoristaRequestDTO;
import com.ovg.transportes.dto.MotoristaResponseDTO;
import com.ovg.transportes.dto.PaginaDTO;
import com.ovg.transportes.service.MotoristaService;

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
@RequestMapping("/api/motoristas")
public class MotoristaController {

    private final MotoristaService motoristaService;

    public MotoristaController(MotoristaService motoristaService) {
        this.motoristaService = motoristaService;
    }

    @GetMapping
    public PaginaDTO<MotoristaResponseDTO> listar(
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamanho
    ) {
        return motoristaService.listarAtivos(PageRequest.of(pagina, tamanho));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TRANSPORTE')")
    public MotoristaResponseDTO cadastrar(@RequestBody @Valid MotoristaRequestDTO requisicao) {
        return motoristaService.cadastrar(requisicao);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public MotoristaResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid MotoristaRequestDTO requisicao) {
        return motoristaService.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TRANSPORTE')")
    public void inativar(@PathVariable Long id) {
        motoristaService.inativar(id);
    }
}
