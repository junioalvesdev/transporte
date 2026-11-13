package com.ovg.transportes.service;

import com.ovg.transportes.dto.local.CidadeResponseDTO;
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

    // "Nome/UF" de uma cidade, pra preencher o campo de cidade de um formulario
    public String nomeComUf(Long id) {
        return id == null ? null : cidadeRepository.findById(id).map(cidade -> cidade.nomeComUf()).orElse(null);
    }

    // todas as cidades como "Nome/UF" — opcoes do datalist do formulario de
    // nova reserva, que substitui a busca com autocompletar feita em JS
    public List<String> listarNomesComUf() {
        return cidadeRepository.listarTodasComEstado().stream()
            .map(cidade -> cidade.nomeComUf())
            .toList();
    }
}
