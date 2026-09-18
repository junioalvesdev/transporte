package com.ovg.transportes.service;

import com.ovg.transportes.dto.BuscaViagemRequestDTO;
import com.ovg.transportes.dto.ViagemDisponivelDTO;
import com.ovg.transportes.model.PontoRota;
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

    public ViagemDisponivelService(ViagemRepository viagemRepository) {
        this.viagemRepository = viagemRepository;
    }

    @Transactional(readOnly = true)
    public List<ViagemDisponivelDTO> buscar(BuscaViagemRequestDTO requisicao) {
        LocalDateTime inicioDia = requisicao.data().atStartOfDay();
        LocalDateTime fimDia = requisicao.data().plusDays(1).atStartOfDay();

        List<Viagem> candidatas = viagemRepository.buscarAbertasNoPeriodo(inicioDia, fimDia);

        return candidatas.stream()
            .filter(viagem -> viagem.temVagaPara(requisicao.qtdPassageiros()))
            .sorted(Comparator.comparing(Viagem::getDataHoraSaida))
            .map(this::paraDto)
            .toList();
    }

    private ViagemDisponivelDTO paraDto(Viagem viagem) {
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
            viagem.vagasDisponiveis()
        );
    }

    // unidades diferentes podem ficar na mesma cidade (ex.: SEDE e CISF, ambas
    // em Goiania) — mostrar so a cidade nesse caso esconderia a diferenca.
    private String nomeExibicao(PontoRota ponto) {
        return ponto.getLocal() != null ? ponto.getLocal().getNome() : ponto.getCidade().getNome();
    }
}
