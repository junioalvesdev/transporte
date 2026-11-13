package com.ovg.transportes.service.tela;

import com.ovg.transportes.common.tela.Formatos;
import com.ovg.transportes.dto.comum.PaginaDTO;
import com.ovg.transportes.dto.solicitacao.SolicitacaoResponseDTO;
import com.ovg.transportes.dto.tela.pendentes.CartaoPendenteDTO;
import com.ovg.transportes.dto.tela.pendentes.ListaPendentesDTO;
import com.ovg.transportes.model.StatusSolicitacao;
import com.ovg.transportes.service.SolicitacaoService;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Regras da pagina "Solicitacoes pendentes": a lista do que ainda precisa de
 * decisao, pronta pra exibir. Aprovar/reprovar ficam no AprovacaoService.
 */
@Service
public class SolicitacoesPendentesService {

    private static final int POR_PAGINA = 10;

    private final SolicitacaoService solicitacaoService;
    private final AprovacaoService aprovacaoService;

    public SolicitacoesPendentesService(SolicitacaoService solicitacaoService, AprovacaoService aprovacaoService) {
        this.solicitacaoService = solicitacaoService;
        this.aprovacaoService = aprovacaoService;
    }

    public ListaPendentesDTO montarLista(int pagina) {
        PaginaDTO<SolicitacaoResponseDTO> resultado = solicitacaoService.listarPorStatus(
            StatusSolicitacao.PENDENTE, PageRequest.of(Math.max(pagina, 0), POR_PAGINA));
        return new ListaPendentesDTO(
            resultado.conteudo().stream().map(SolicitacoesPendentesService::paraCartao).toList(),
            LayoutService.paginacao(resultado, "/solicitacoes-pendentes?pagina="),
            // a viagem nasce com o status padrao (sem o campo "Status da viagem", como era aqui)
            aprovacaoService.formulario(false),
            "pagina=" + resultado.paginaAtual()
        );
    }

    private static CartaoPendenteDTO paraCartao(SolicitacaoResponseDTO solicitacao) {
        String modalAprovarId = "aprovar-" + solicitacao.id();
        String modalReprovarId = "reprovar-" + solicitacao.id();
        return new CartaoPendenteDTO(
            solicitacao.id(),
            solicitacao.origemExibicao() + " → " + solicitacao.destinoExibicao(),
            "%s · %d passageiro(s)".formatted(Formatos.dataHora(solicitacao.dataHoraDesejada()), solicitacao.qtdPassageiros()),
            solicitacao.urgente(),
            solicitacao.solicitanteNome(),
            solicitacao.telefoneContato() == null || solicitacao.telefoneContato().isBlank() ? null : Formatos.telefone(solicitacao.telefoneContato()),
            solicitacao.finalidade() == null || solicitacao.finalidade().isBlank() ? null : solicitacao.finalidade(),
            Formatos.campoDataHora(solicitacao.dataHoraDesejada()),
            Formatos.campoDataHora(solicitacao.dataHoraRetornoDesejada()),
            modalAprovarId,
            "#" + modalAprovarId,
            modalReprovarId,
            "#" + modalReprovarId,
            "/solicitacoes-pendentes/" + solicitacao.id() + "/aprovar",
            "/solicitacoes-pendentes/" + solicitacao.id() + "/reprovar"
        );
    }
}
