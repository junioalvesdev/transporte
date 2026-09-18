package com.ovg.transportes.service;

import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.MotoristaRequestDTO;
import com.ovg.transportes.dto.MotoristaResponseDTO;
import com.ovg.transportes.dto.PaginaDTO;
import com.ovg.transportes.model.LocalAdministrativo;
import com.ovg.transportes.model.Motorista;
import com.ovg.transportes.repository.LocalAdministrativoRepository;
import com.ovg.transportes.repository.MotoristaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final LocalAdministrativoRepository localAdministrativoRepository;

    public MotoristaService(MotoristaRepository motoristaRepository, LocalAdministrativoRepository localAdministrativoRepository) {
        this.motoristaRepository = motoristaRepository;
        this.localAdministrativoRepository = localAdministrativoRepository;
    }

    public PaginaDTO<MotoristaResponseDTO> listarAtivos(Pageable pageable) {
        return PaginaDTO.de(motoristaRepository.findByAtivoTrueOrderByIdDesc(pageable), MotoristaResponseDTO::de);
    }

    @Transactional
    public MotoristaResponseDTO cadastrar(MotoristaRequestDTO requisicao) {
        Motorista motorista = new Motorista(requisicao.nome(), requisicao.telefone(), buscarLocalSeInformado(requisicao.localId()));
        return MotoristaResponseDTO.de(motoristaRepository.save(motorista));
    }

    @Transactional
    public MotoristaResponseDTO atualizar(Long id, MotoristaRequestDTO requisicao) {
        Motorista motorista = buscarPorId(id);
        motorista.atualizar(requisicao.nome(), requisicao.telefone(), buscarLocalSeInformado(requisicao.localId()));
        return MotoristaResponseDTO.de(motorista);
    }

    @Transactional
    public void inativar(Long id) {
        buscarPorId(id).inativar();
    }

    private Motorista buscarPorId(Long id) {
        return motoristaRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista nao encontrado: " + id));
    }

    private LocalAdministrativo buscarLocalSeInformado(Long id) {
        if (id == null) {
            return null;
        }
        return localAdministrativoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Local administrativo nao encontrado: " + id));
    }
}
