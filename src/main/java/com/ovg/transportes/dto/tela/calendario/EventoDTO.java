package com.ovg.transportes.dto.tela.calendario;

/** Uma "pilula" colorida no calendario (viagem ou solicitacao) que abre o modal de detalhe. */
public record EventoDTO(
    String texto,       // "08:00 Visita domiciliar"
    String textoDia,    // versao mais completa, usada na lista "+N mais" do dia
    String dica,        // tooltip ao passar o mouse
    String classe,      // "evento-pilula cor-aberta urgente"
    String alvoModal    // "#viagem-12" ou "#solicitacao-34"
) {
}
