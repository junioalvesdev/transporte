package com.ovg.transportes.dto.tela.reservas;

/**
 * Uma reserva da tabela (e do modal de detalhe), ja pronta pra exibir.
 * Montada pelo ReservasService; no HTML e so ${r.campo}.
 */
public record LinhaReservaDTO(
    Long id,
    String modalId,         // id do modal de detalhe desta reserva
    String alvoModal,       // "#" + modalId, pros botoes que abrem o modal
    String requerente,
    String telefone,        // ja formatado: (62) 99999-9999
    String titulo,
    boolean urgente,
    String origem,
    String destino,
    String partida,         // dd/MM/yyyy HH:mm
    String retorno,         // dd/MM/yyyy HH:mm, ou "-"
    String partidaCampo,    // valor inicial do campo de saida ao aprovar
    String retornoCampo,    // valor inicial do campo de retorno ao aprovar
    String status,          // "Aguardando", "Disponivel"...
    String classeStatus,    // classes do selo colorido do status
    Integer usuarios,
    String veiculo,         // placa, ou "N/A"
    String descricao,       // observacoes, senao o titulo
    String motivoRecusa,
    String observacoes,
    boolean pendente,       // so pendente mostra aprovar/reprovar
    String urlAprovar,
    String urlReprovar
) {
}
