package com.ovg.transportes.controller;

import com.ovg.transportes.dto.LocalAdministrativoResponseDTO;
import com.ovg.transportes.service.LocalAdministrativoService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locais-administrativos")
public class LocalAdministrativoController {

    private final LocalAdministrativoService localAdministrativoService;

    public LocalAdministrativoController(LocalAdministrativoService localAdministrativoService) {
        this.localAdministrativoService = localAdministrativoService;
    }

    @GetMapping
    public List<LocalAdministrativoResponseDTO> listar() {
        return localAdministrativoService.listar();
    }
}
