package com.ovg.transportes.dto.tela.reservas;

import com.ovg.transportes.common.tela.Opcao;
import com.ovg.transportes.dto.tela.formularios.FormAprovarDTO;
import com.ovg.transportes.dto.tela.formularios.FormNovaReservaDTO;
import com.ovg.transportes.dto.tela.formularios.FormOrdemTrafegoDTO;

import java.util.List;

/**
 * Tudo que a pagina "Lista de Reservas" mostra, montado pelo ReservasService.
 * O controller manda isto pro HTML como "pagina" (${pagina.grupos}, ${pagina.resumoFiltros}...).
 */
public record ListaReservasDTO(
    // tabela e barra de ferramentas
    List<GrupoMesDTO> grupos,
    String resumoFiltros,           // "Filtros: 2"
    String resumoPeriodo,           // "Setembro 2026 · 355 reserva(s)"
    boolean atingiuLimite,
    String queryFiltros,            // "status=PENDENTE&de=..." pra voltar pra mesma lista filtrada
    String urlLista,
    String classeBody,              // "modal-open" quando o modal de nova reserva volta aberto

    // modal de filtros
    List<Opcao> opcoesStatus,
    String filtroDe,
    String filtroAte,

    // formularios dos modais
    FormAprovarDTO formAprovar,
    FormNovaReservaDTO formNovaReserva,
    FormOrdemTrafegoDTO formOrdemTrafego
) {
}
