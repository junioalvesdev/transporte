package com.ovg.transportes.dto.tela.motoristas;

/** Um motorista da tabela, com o que os modais de editar e inativar dele precisam. */
public record LinhaMotoristaDTO(
    Long id,
    String nome,
    String telefone,            // como foi cadastrado (campo livre), ou vazio
    String tituloEditar,        // "Editar motorista — Fulano"
    String modalEditarId,
    String alvoModalEditar,     // "#" + modalEditarId
    String modalInativarId,
    String alvoModalInativar,
    String urlEditar,
    String urlInativar
) {
}
