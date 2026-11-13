package com.ovg.transportes.dto.tela.veiculos;

import com.ovg.transportes.common.tela.Opcao;

import java.util.List;

/** Um veiculo da tabela, com o que os modais de editar e inativar dele precisam. */
public record LinhaVeiculoDTO(
    Long id,
    String placa,
    String modelo,
    Integer capacidade,
    String tipo,
    String iconeTipo,           // classe do icone do tipo (bi bi-truck...)
    String tituloEditar,        // "Editar veiculo — ABC1D23"
    String modalEditarId,
    String alvoModalEditar,     // "#" + modalEditarId
    String modalInativarId,
    String alvoModalInativar,
    String urlEditar,
    String urlInativar,
    List<Opcao> tipos           // select de tipo ja com o tipo atual marcado
) {
}
