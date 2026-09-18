package com.ovg.transportes.service;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.AvaliacaoRequestDTO;
import com.ovg.transportes.model.Avaliacao;
import com.ovg.transportes.model.ViagemParticipante;
import com.ovg.transportes.repository.AvaliacaoRepository;
import com.ovg.transportes.repository.ViagemParticipanteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ViagemParticipanteRepository viagemParticipanteRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, ViagemParticipanteRepository viagemParticipanteRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.viagemParticipanteRepository = viagemParticipanteRepository;
    }

    @Transactional
    public void avaliar(Long viagemParticipanteId, AvaliacaoRequestDTO requisicao) {
        if (avaliacaoRepository.existsByViagemParticipanteId(viagemParticipanteId)) {
            throw new NegocioException("Esta participacao ja foi avaliada");
        }
        ViagemParticipante participante = viagemParticipanteRepository.findById(viagemParticipanteId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Participacao nao encontrada: " + viagemParticipanteId));

        avaliacaoRepository.save(new Avaliacao(participante, requisicao.nota(), requisicao.categoria(), requisicao.comentario()));
    }
}
