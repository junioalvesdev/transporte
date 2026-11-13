package com.ovg.transportes.dto.tela.calendario;

import com.ovg.transportes.common.tela.Opcao;

import java.util.List;

/**
 * Uma viagem pro modal de detalhe do calendario: o resumo ja formatado e,
 * pra quem pode, o formulario de edicao ja preenchido.
 */
public record ViagemDetalheDTO(
    String modalId,                 // "viagem-12"

    // resumo (null esconde a linha)
    String solicitantes,            // quem pediu a viagem ("Fulano, Beltrano" ou "-")
    List<String> caronas,           // quem entrou depois pela busca de caronas
    String telefones,
    String titulo,
    String origem,
    String destino,
    String saida,
    String retorno,
    String ocupacao,                // "2/4"
    String veiculo,
    String motorista,
    String status,
    String classeStatus,            // "evento-pilula cor-aberta"
    String observacoes,

    boolean passada,                // ja ocorreu: nada pode ser alterado
    boolean podeEditar,             // TRANSPORTE numa viagem de hoje pra frente
    boolean podeCancelarParticipacao, // quem pegou vaga nela (nao TRANSPORTE)

    // formulario de edicao
    String urlEditar,
    String formEditarId,            // id do form (o botao "Salvar" do rodape envia ele)
    String statusAtual,             // campo escondido: o codigo do status antes de editar
    List<Opcao> veiculos,           // ja com o veiculo atual marcado
    List<Opcao> motoristas,
    String saidaCampo,
    String retornoCampo,
    Integer capacidade,
    Integer capacidadeMinima,       // nao da pra ficar abaixo da ocupacao atual
    String rotuloCapacidade,        // "Capacidade (ocupacao atual: 2/4)"
    List<Opcao> statusOpcoes,

    // cancelar a propria participacao
    String urlCancelarParticipacao
) {
}
