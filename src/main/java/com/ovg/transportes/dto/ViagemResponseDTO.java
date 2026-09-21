package com.ovg.transportes.dto;

import com.ovg.transportes.model.PontoRota;
import com.ovg.transportes.model.StatusParticipante;
import com.ovg.transportes.model.StatusViagem;
import com.ovg.transportes.model.TipoPonto;
import com.ovg.transportes.model.Viagem;

import java.time.LocalDateTime;
import java.util.List;

public record ViagemResponseDTO(
    Long id,
    Long veiculoId,
    String veiculoPlaca,
    Long motoristaId,
    String motoristaNome,
    LocalDateTime dataHoraSaida,
    LocalDateTime dataHoraChegadaEstimada,
    Integer capacidadeTotal,
    int vagasOcupadas,
    StatusViagem status,
    List<String> solicitantesNomes,
    String unidadeOrigem,
    String finalidade,
    boolean urgente,
    String origemExibicao,
    String destinoExibicao,
    Long minhaSolicitacaoId,
    List<String> telefonesContato,
    List<String> observacoes
) {

    public static ViagemResponseDTO de(Viagem viagem) {
        return de(viagem, null);
    }

    // minhaSolicitacaoId: a solicitacao do usuario logado dentro desta viagem,
    // se houver — usado pelo calendario pra mostrar "Cancelar" pra quem nao e
    // do Departamento de Transportes (nunca null quando vem de listarNoPeriodo)
    public static ViagemResponseDTO de(Viagem viagem, Long minhaSolicitacaoId) {
        List<String> solicitantesNomes = viagem.getParticipantes().stream()
            .filter(participante -> participante.getStatus() != StatusParticipante.CANCELADO)
            .map(participante -> participante.getSolicitacao().getSolicitante().getNome())
            .distinct()
            .toList();

        // varias solicitacoes podem compartilhar a mesma viagem; pro calendario,
        // mostra so a finalidade da primeira (nao ha espaco pra listar todas)
        String finalidade = viagem.getParticipantes().stream()
            .filter(participante -> participante.getStatus() != StatusParticipante.CANCELADO)
            .map(participante -> participante.getSolicitacao().getFinalidade())
            .filter(texto -> texto != null && !texto.isBlank())
            .findFirst()
            .orElse(null);

        // basta uma das solicitacoes vinculadas ser urgente pra viagem toda
        // ganhar o destaque de urgencia no calendario
        boolean urgente = viagem.getParticipantes().stream()
            .filter(participante -> participante.getStatus() != StatusParticipante.CANCELADO)
            .anyMatch(participante -> participante.getSolicitacao().isUrgente());

        List<String> telefonesContato = viagem.getParticipantes().stream()
            .filter(participante -> participante.getStatus() != StatusParticipante.CANCELADO)
            .map(participante -> participante.getSolicitacao().getTelefoneContato())
            .filter(texto -> texto != null && !texto.isBlank())
            .distinct()
            .toList();

        List<String> observacoes = viagem.getParticipantes().stream()
            .filter(participante -> participante.getStatus() != StatusParticipante.CANCELADO)
            .map(participante -> participante.getSolicitacao().getObservacoes())
            .filter(texto -> texto != null && !texto.isBlank())
            .distinct()
            .toList();

        // usado pra pre-preencher a "Unidade" na Ordem de Trafego quando a rota veio de uma unidade administrativa
        String unidadeOrigem = viagem.getRota() != null && !viagem.getRota().getPontos().isEmpty()
            && viagem.getRota().getPontos().get(0).getLocal() != null
            ? viagem.getRota().getPontos().get(0).getLocal().getNome()
            : null;

        List<PontoRota> pontos = viagem.getRota() != null ? viagem.getRota().getPontos() : List.of();
        String origemExibicao = exibirPonto(pontos, TipoPonto.ORIGEM);
        String destinoExibicao = exibirPonto(pontos, TipoPonto.DESTINO);

        return new ViagemResponseDTO(
            viagem.getId(),
            viagem.getVeiculo().getId(),
            viagem.getVeiculo().getPlaca(),
            viagem.getMotorista().getId(),
            viagem.getMotorista().getNome(),
            viagem.getDataHoraSaida(),
            viagem.getDataHoraChegadaEstimada(),
            viagem.getCapacidadeTotal(),
            viagem.vagasOcupadas(),
            viagem.getStatus(),
            solicitantesNomes,
            unidadeOrigem,
            finalidade,
            urgente,
            origemExibicao,
            destinoExibicao,
            minhaSolicitacaoId,
            telefonesContato,
            observacoes
        );
    }

    private static String exibirPonto(List<PontoRota> pontos, TipoPonto tipo) {
        return pontos.stream()
            .filter(ponto -> ponto.getTipoPonto() == tipo)
            .findFirst()
            .map(ponto -> ponto.getLocal() != null ? ponto.getLocal().getNome() : ponto.getCidade().nomeComUf())
            .orElse(null);
    }
}
