package com.ovg.transportes.dto.tela.calendario;

/** Uma viagem encontrada na busca de carona (cartao do modal de resultados). */
public record CartaoCaronaDTO(
    String rota,                // "SEDE → PJTF"
    String data,                // "24/09/2026"
    String horarios,            // "Saida: 08:00 · Chegada estimada: 17:00"
    String tipoVeiculo,
    String iconeTipo,
    String vagas,               // "3 lugares disponiveis"
    String classeVagas,         // "badge badge-vagas baixa mb-2 d-inline-block" (baixa = poucas vagas)
    String classeCartao,        // destaca quando a pessoa ja pegou vaga
    boolean jaSolicitei,
    String urlSolicitar,        // form: pede a vaga
    Long cidadeEmbarqueId,
    Long cidadeDesembarqueId,
    String urlCancelar          // form: cancela a propria vaga
) {
}
