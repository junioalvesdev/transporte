package com.ovg.transportes.service;

import com.ovg.transportes.dto.FeedbackRequestDTO;
import com.ovg.transportes.model.Feedback;
import com.ovg.transportes.model.Usuario;
import com.ovg.transportes.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public FeedbackService(FeedbackRepository feedbackRepository, UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.feedbackRepository = feedbackRepository;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @Transactional
    public void enviar(FeedbackRequestDTO requisicao) {
        Usuario usuario = usuarioAutenticadoProvider.obterUsuarioLogado();
        feedbackRepository.save(new Feedback(usuario, requisicao.tipo(), requisicao.descricao()));
    }
}
