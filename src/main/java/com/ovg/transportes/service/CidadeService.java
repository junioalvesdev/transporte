package com.ovg.transportes.service;

import com.ovg.transportes.dto.CidadeResponseDTO;
import com.ovg.transportes.repository.CidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CidadeService {

    private static final int LIMITE_RESULTADOS = 20;

    private final CidadeRepository cidadeRepository;

    public CidadeService(CidadeRepository cidadeRepository) {
        this.cidadeRepository = cidadeRepository;
    }

    public List<CidadeResponseDTO> buscarPorNome(String nome) {
        return cidadeRepository.buscarPorNome(nome).stream()
            .limit(LIMITE_RESULTADOS)
            .map(CidadeResponseDTO::de)
            .toList();
    }
}
