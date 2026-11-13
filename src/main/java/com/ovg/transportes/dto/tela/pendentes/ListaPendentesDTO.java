package com.ovg.transportes.dto.tela.pendentes;

import com.ovg.transportes.dto.tela.formularios.FormAprovarDTO;
import com.ovg.transportes.dto.tela.layout.PaginacaoDTO;

import java.util.List;

/** Tudo que a pagina "Solicitacoes pendentes" mostra (vai pro HTML como ${pagina}). */
public record ListaPendentesDTO(
    List<CartaoPendenteDTO> cartoes,
    PaginacaoDTO paginacao,
    FormAprovarDTO formAprovar,
    String filtros              // "pagina=2": pra voltar pra mesma pagina depois de aprovar/reprovar
) {
}
