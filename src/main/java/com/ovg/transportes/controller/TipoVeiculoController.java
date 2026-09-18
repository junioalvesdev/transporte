package com.ovg.transportes.controller;

import com.ovg.transportes.dto.TipoVeiculoResponseDTO;
import com.ovg.transportes.service.TipoVeiculoService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-veiculo")
public class TipoVeiculoController {

    private final TipoVeiculoService tipoVeiculoService;

    public TipoVeiculoController(TipoVeiculoService tipoVeiculoService) {
        this.tipoVeiculoService = tipoVeiculoService;
    }

    @GetMapping
    public List<TipoVeiculoResponseDTO> listar() {
        return tipoVeiculoService.listar();
    }
}
