package com.ovg.transportes.dto.tela.minhas;

/** Uma solicitacao de quem esta logado (cartao da lista "Minhas solicitacoes"). */
public record CartaoMinhaSolicitacaoDTO(
    Long id,
    String rota,                // "SEDE → PJTF"
    String resumo,              // "24/09/2026 08:00 · 2 passageiro(s)"
    String motivoRecusa,        // ou null (esconde a linha)
    String status,              // "Aguardando", "Disponivel"...
    String classeStatus,        // classes do selo colorido
    boolean podeCancelar,       // pendente/aprovada e antes do horario
    boolean prazoEncerrado,     // pendente/aprovada, mas o horario ja passou
    String modalCancelarId,
    String alvoModalCancelar,   // "#" + modalCancelarId
    String urlCancelar
) {
}
