package com.ovg.transportes.dto.tela.pendentes;

/** Uma solicitacao pendente (cartao da lista), com o que os modais de aprovar e reprovar precisam. */
public record CartaoPendenteDTO(
    Long id,
    String rota,                // "SEDE → PJTF"
    String resumo,              // "24/09/2026 08:00 · 2 passageiro(s)"
    boolean urgente,
    String solicitante,
    String telefone,            // ja formatado, ou null (esconde a linha)
    String finalidade,          // ou null (esconde a linha)
    String partidaCampo,        // valores iniciais dos campos de data ao aprovar
    String retornoCampo,
    String modalAprovarId,
    String alvoModalAprovar,    // "#" + modalAprovarId
    String modalReprovarId,
    String alvoModalReprovar,
    String urlAprovar,
    String urlReprovar
) {
}
