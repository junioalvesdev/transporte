package com.ovg.transportes.service;

import com.ovg.transportes.dto.TipoVeiculoResponseDTO;
import com.ovg.transportes.repository.TipoVeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TipoVeiculoService {

    private final TipoVeiculoRepository tipoVeiculoRepository;

    public TipoVeiculoService(TipoVeiculoRepository tipoVeiculoRepository) {
        this.tipoVeiculoRepository = tipoVeiculoRepository;
    }

    public List<TipoVeiculoResponseDTO> listar() {
        return tipoVeiculoRepository.findAll().stream()
            .map(TipoVeiculoResponseDTO::de)
            .toList();
    }
}
