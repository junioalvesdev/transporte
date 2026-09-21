package com.ovg.transportes.service;

import com.ovg.transportes.dto.BuscaViagemRequestDTO;
import com.ovg.transportes.dto.ViagemDisponivelDTO;
import com.ovg.transportes.model.PontoRota;
import com.ovg.transportes.model.StatusParticipante;
import com.ovg.transportes.model.Viagem;
import com.ovg.transportes.repository.ViagemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Implementa a tela "Viagens disponiveis": lista tudo que esta aberto para
 * aproveitamento no DIA escolhido, com vaga suficiente. Sem filtro de
 * origem/destino — ver BuscaViagemRequestDTO.
 */
@Service
public class ViagemDisponivelService {

    private final ViagemRepository viagemRepository;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public ViagemDisponivelService(ViagemRepository viagemRepository, UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.viagemRepository = viagemRepository;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @Transactional(readOnly = true)
    public List<ViagemDisponivelDTO> buscar(BuscaViagemRequestDTO requisicao) {
        LocalDateTime inicioDia = requisicao.data().atStartOfDay();
        LocalDateTime fimDia = requisicao.data().plusDays(1).atStartOfDay();
        Long usuarioLogadoId = usuarioAutenticadoProvider.obterUsuarioLogado().getId();

        List<Viagem> candidatas = viagemRepository.buscarAbertasNoPeriodo(inicioDia, fimDia);

        // uma viagem que a pessoa ja garantiu vaga continua aparecendo pra ela
        // mesmo que tenha lotado depois (senao o card "some" bem na hora que
        // ela confirma a propria vaga, o que parece um bug)
        return candidatas.stream()
            .filter(viagem -> viagem.temVagaPara(requisicao.qtdPassageiros()) || minhaSolicitacaoNaViagem(viagem, usuarioLogadoId) != null)
            .sorted(Comparator.comparing(Viagem::getDataHoraSaida))
            .map(viagem -> paraDto(viagem, usuarioLogadoId))
            .toList();
    }

    // solicitacao do usuario logado dentro desta viagem, se houver — usado pra
    // mostrar "Vaga solicitada" (com opcao de cancelar) em vez do botao de pedir
    private Long minhaSolicitacaoNaViagem(Viagem viagem, Long usuarioLogadoId) {
        return viagem.getParticipantes().stream()
            .filter(participante -> participante.getStatus() != StatusParticipante.CANCELADO)
            .filter(participante -> participante.getSolicitacao().getSolicitante().getId().equals(usuarioLogadoId))
            .map(participante -> participante.getSolicitacao().getId())
            .findFirst()
            .orElse(null);
    }

    private ViagemDisponivelDTO paraDto(Viagem viagem, Long usuarioLogadoId) {
        List<PontoRota> pontos = viagem.getRota().getPontos();
        PontoRota origem = pontos.get(0);
        PontoRota destino = pontos.getLast();

        return new ViagemDisponivelDTO(
            viagem.getId(),
            origem.getCidade().getId(),
            nomeExibicao(origem),
            destino.getCidade().getId(),
            nomeExibicao(destino),
            viagem.getDataHoraSaida(),
            viagem.getDataHoraChegadaEstimada(),
            viagem.getVeiculo().getTipoVeiculo().getNome(),
            viagem.vagasDisponiveis(),
            minhaSolicitacaoNaViagem(viagem, usuarioLogadoId)
        );
    }

    // unidades diferentes podem ficar na mesma cidade (ex.: SEDE e CISF, ambas
    // em Goiania) — mostrar so a cidade nesse caso esconderia a diferenca.
    private String nomeExibicao(PontoRota ponto) {
        return ponto.getLocal() != null ? ponto.getLocal().getNome() : ponto.getCidade().getNome();
    }
}
