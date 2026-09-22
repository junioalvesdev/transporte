package com.ovg.transportes.dto;

import com.ovg.transportes.model.LocalAdministrativo;
import com.ovg.transportes.model.Solicitacao;
import com.ovg.transportes.model.StatusSolicitacao;

import java.time.LocalDateTime;

public record SolicitacaoResponseDTO(
    Long id,
    String solicitanteNome,
    String solicitanteLogin,
    Long cidadeOrigemId,
    Long cidadeDestinoId,
    Long localOrigemId,
    Long localDestinoId,
    String origemExibicao,
    String destinoExibicao,
    LocalDateTime dataHoraDesejada,
    LocalDateTime dataHoraRetornoDesejada,
    Integer qtdPassageiros,
    String telefoneContato,
    String finalidade,
    String observacoes,
    boolean urgente,
    StatusSolicitacao status,
    String motivoRecusa,
    String veiculoPlaca
) {

    public static SolicitacaoResponseDTO de(Solicitacao solicitacao) {
        return de(solicitacao, null);
    }

    // veiculoPlaca so existe quando a solicitacao ja foi aprovada e vinculada a
    // uma viagem (ver ViagemParticipanteRepository.findBySolicitacaoId) — fica
    // null pra uma solicitacao recem-criada, pendente, reprovada ou cancelada
    public static SolicitacaoResponseDTO de(Solicitacao solicitacao, String veiculoPlaca) {
        return new SolicitacaoResponseDTO(
            solicitacao.getId(),
            solicitacao.getSolicitante().getNome(),
            solicitacao.getSolicitante().getLoginAd(),
            solicitacao.getCidadeOrigem().getId(),
            solicitacao.getCidadeDestino().getId(),
            solicitacao.getLocalOrigem() != null ? solicitacao.getLocalOrigem().getId() : null,
            solicitacao.getLocalDestino() != null ? solicitacao.getLocalDestino().getId() : null,
            exibir(solicitacao.getLocalOrigem(), solicitacao.getCidadeOrigem().nomeComUf(), solicitacao.getDescricaoOrigem()),
            exibir(solicitacao.getLocalDestino(), solicitacao.getCidadeDestino().nomeComUf(), solicitacao.getDescricaoDestino()),
            solicitacao.getDataHoraDesejada(),
            solicitacao.getDataHoraRetornoDesejada(),
            solicitacao.getQtdPassageiros(),
            solicitacao.getTelefoneContato(),
            solicitacao.getFinalidade(),
            solicitacao.getObservacoes(),
            solicitacao.isUrgente(),
            solicitacao.getStatus(),
            solicitacao.getMotivoRecusa(),
            veiculoPlaca
        );
    }

    // unidade administrativa -> mostra so o nome dela (ex.: "SEDE"); cidade "outro" ->
    // mostra a cidade e, se houver, a descricao do endereco entre parenteses.
    private static String exibir(com.ovg.transportes.model.LocalAdministrativo local, String cidadeNomeComUf, String descricao) {
        if (local != null) {
            return local.getNome();
        }
        return descricao == null || descricao.isBlank()
            ? cidadeNomeComUf
            : "%s (%s)".formatted(cidadeNomeComUf, descricao);
    }
}
