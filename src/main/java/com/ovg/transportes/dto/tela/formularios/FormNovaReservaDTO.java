package com.ovg.transportes.dto.tela.formularios;

import com.ovg.transportes.common.tela.Opcao;

import java.util.List;

/**
 * Modal "Criar Reservas" pronto pra exibir (templates/fragmentos/modal-nova-reserva.html).
 * Usado na Lista de Reservas e no Calendario — cada pagina diz pra onde o
 * formulario envia (acao). Os valores vem em branco na primeira abertura, ou
 * com o que a pessoa digitou quando o envio deu erro (e o modal volta aberto).
 */
public record FormNovaReservaDTO(
    String acao,                    // pra onde o form envia: "/reservas/nova", "/calendario/nova"
    String filtros,                 // campo escondido: pra voltar pra mesma tela depois de enviar
    String classeModal,             // "modal fade" ou, se voltou com erro, "modal fade show d-block"
    boolean aberto,                 // voltou com erro: fechar vira link, e a pagina ganha o fundo escuro
    String erro,                    // mensagem do erro do ultimo envio, ou null
    String urlFechar,               // pra onde o "fechar" leva quando o modal voltou aberto

    // valores dos campos
    String finalidade,
    String cidadeOrigem,
    String descricaoOrigem,
    String cidadeDestino,
    String descricaoDestino,
    String saidaCampo,              // "2026-10-20T08:00" (formato do datetime-local)
    String retornoCampo,
    List<String> camposReplicar,    // sempre 5 campos de data, preenchidos ou vazios
    String classeReplicar,          // "replicar para outros dias" ja vem aberto se algum dia foi preenchido
    Integer qtdPassageiros,
    String telefone,
    String observacoes,

    // opcoes dos selects
    List<Opcao> opcoesOrigem,       // unidades da OVG + "Outro local"
    List<Opcao> opcoesDestino,
    List<String> cidades            // lista de sugestoes (datalist): "Goiania/GO"...
) {
}
