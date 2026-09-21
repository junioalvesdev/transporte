package com.ovg.transportes.service;

import com.ovg.transportes.dto.ContagemDTO;
import com.ovg.transportes.dto.RelatorioMensalDTO;
import com.ovg.transportes.repository.RelatorioRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;

    public RelatorioService(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    @PreAuthorize("hasRole('TRANSPORTE')")
    public RelatorioMensalDTO gerar(LocalDateTime inicio, LocalDateTime fim) {
        return new RelatorioMensalDTO(
            relatorioRepository.contarTotal(inicio, fim),
            relatorioRepository.contarPorMotorista(inicio, fim),
            relatorioRepository.contarPorTipoVeiculo(inicio, fim),
            relatorioRepository.contarPorStatus(inicio, fim),
            mapearPorMes(relatorioRepository.contarPorMesBruto(inicio, fim))
        );
    }

    private List<ContagemDTO> mapearPorMes(List<Object[]> linhasBrutas) {
        // cada linha vem do banco como Object[2] (rotulo, quantidade); convertida
        // aqui para o DTO tipado que o resto do sistema usa.
        return linhasBrutas.stream()
            .map(linha -> new ContagemDTO((String) linha[0], ((Number) linha[1]).longValue()))
            .toList();
    }
}
