package com.ovg.transportes.dto.tela.calendario;

import com.ovg.transportes.common.tela.Opcao;

import java.util.List;

/**
 * Uma solicitacao que ainda nao virou viagem (pendente, reprovada ou
 * cancelada) pro modal de detalhe do calendario. Se estiver pendente:
 * TRANSPORTE aprova/reprova; quem pediu edita os dados ou cancela.
 */
public record SolicitacaoDetalheDTO(
    String modalId,                 // "solicitacao-34"

    // resumo (null esconde a linha)
    String solicitante,
    String telefone,
    String titulo,
    String origem,
    String destino,
    String data,
    String retorno,
    Integer passageiros,
    String status,
    String classeStatus,
    String observacoes,
    String motivoRecusa,

    boolean podeAprovar,            // pendente e quem ve e TRANSPORTE
    boolean podeEditar,             // pendente e quem ve e quem pediu

    // aprovar / reprovar (TRANSPORTE)
    String urlAprovar,
    String urlReprovar,
    String saidaCampo,              // valores iniciais dos campos de data
    String retornoCampo,

    // editar / cancelar (quem pediu)
    String urlEditar,
    String formEditarId,            // id do form (o botao "Salvar" do rodape envia ele)
    String urlCancelar,
    String finalidade,
    List<Opcao> opcoesOrigem,       // unidades + "Outro local", com a atual marcada
    String cidadeOrigem,            // "Goiania/GO" quando e "outro local"
    String descricaoOrigem,
    List<Opcao> opcoesDestino,
    String cidadeDestino,
    String descricaoDestino,
    String telefoneCampo,
    String observacoesCampo
) {
}
