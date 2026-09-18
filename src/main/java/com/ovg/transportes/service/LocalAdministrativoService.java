package com.ovg.transportes.service;

import com.ovg.transportes.dto.LocalAdministrativoResponseDTO;
import com.ovg.transportes.repository.LocalAdministrativoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LocalAdministrativoService {

    private final LocalAdministrativoRepository localAdministrativoRepository;

    public LocalAdministrativoService(LocalAdministrativoRepository localAdministrativoRepository) {
        this.localAdministrativoRepository = localAdministrativoRepository;
    }

    public List<LocalAdministrativoResponseDTO> listar() {
        return localAdministrativoRepository.listarComCidade().stream()
            .map(LocalAdministrativoResponseDTO::de)
            .toList();
    }
}
