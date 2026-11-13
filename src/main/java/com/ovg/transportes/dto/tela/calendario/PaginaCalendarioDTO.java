package com.ovg.transportes.dto.tela.calendario;

import com.ovg.transportes.dto.tela.formularios.FormAprovarDTO;
import com.ovg.transportes.dto.tela.formularios.FormNovaReservaDTO;
import com.ovg.transportes.dto.tela.formularios.FormOrdemTrafegoDTO;

import java.util.List;

/**
 * Tudo que a pagina "Calendario" mostra, montado pelo CalendarioService.
 * Vai pro HTML como ${pagina}.
 */
public record PaginaCalendarioDTO(
    // cabecalho e navegacao (links prontos: cada clique recarrega a pagina)
    String titulo,                  // "Setembro de 2026" ou "20/09/2026 — 26/09/2026"
    String urlAnterior,
    String urlProximo,
    String urlHoje,
    String urlMes,
    String urlSemana,
    String classeBotaoMes,          // "btn btn-ovg-secundario ativo" no modo atual
    String classeBotaoSemana,
    String urlAtual,                // o calendario exatamente como esta (pra voltar depois de uma acao)
    String filtros,                 // a mesma coisa, so a parte depois do "?"
    boolean ehTransporte,           // mostra "Gerar Ordem de Trafego"
    String classeBody,              // "modal-open" quando algum modal volta aberto

    // grade
    boolean visaoMes,
    List<String> diasSemana,        // "Dom", "Seg"...
    List<DiaCalendarioDTO> dias,    // 42 no mes, 7 na semana

    // lateral
    String tituloMini,              // "Setembro de 2026"
    List<MiniDiaDTO> miniDias,
    List<FiltroStatusDTO> filtroStatus,
    String modoAtual,               // campos escondidos do form de filtro
    String dataAtual,

    // modais de detalhe
    List<ViagemDetalheDTO> viagens,
    List<SolicitacaoDetalheDTO> solicitacoes,

    // modais compartilhados com outras telas
    FormAprovarDTO formAprovar,             // null pra quem nao e TRANSPORTE
    FormNovaReservaDTO formNovaReserva,
    FormOrdemTrafegoDTO formOrdemTrafego,   // null pra quem nao e TRANSPORTE

    // busca de carona
    BuscaCaronaDTO busca,
    List<String> cidades                    // sugestoes de cidade (edicao de solicitacao)
) {
}
