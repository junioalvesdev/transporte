package com.ovg.transportes.service.tela;

import com.ovg.transportes.common.tela.Formatos;
import com.ovg.transportes.common.tela.StatusSolicitacaoTela;
import com.ovg.transportes.dto.comum.PaginaDTO;
import com.ovg.transportes.dto.solicitacao.SolicitacaoResponseDTO;
import com.ovg.transportes.dto.tela.minhas.CartaoMinhaSolicitacaoDTO;
import com.ovg.transportes.dto.tela.minhas.ListaMinhasSolicitacoesDTO;
import com.ovg.transportes.model.StatusSolicitacao;
import com.ovg.transportes.service.SolicitacaoService;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Regras da pagina "Minhas solicitacoes": as solicitacoes de quem esta logado,
 * da mais recente pra mais antiga, com a opcao de cancelar.
 */
@Service
public class MinhasSolicitacoesService {

    private static final int POR_PAGINA = 10;

    private final SolicitacaoService solicitacaoService;

    public MinhasSolicitacoesService(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    public ListaMinhasSolicitacoesDTO montarLista(int pagina) {
        PaginaDTO<SolicitacaoResponseDTO> resultado = solicitacaoService.listarMinhas(PageRequest.of(Math.max(pagina, 0), POR_PAGINA));
        return new ListaMinhasSolicitacoesDTO(
            resultado.conteudo().stream().map(MinhasSolicitacoesService::paraCartao).toList(),
            LayoutService.paginacao(resultado, "/minhas-solicitacoes?pagina="),
            resultado.paginaAtual()
        );
    }

    public void cancelar(Long id) {
        // so o proprio solicitante pode cancelar (a regra fica no SolicitacaoService)
        solicitacaoService.cancelar(id);
    }

    private static CartaoMinhaSolicitacaoDTO paraCartao(SolicitacaoResponseDTO solicitacao) {
        // mesma regra do backend (Solicitacao.cancelar): pendente ou aprovada, e
        // so antes do horario — aqui so deixa isso visivel antes do clique
        boolean statusCancelavel = solicitacao.status() == StatusSolicitacao.PENDENTE
            || solicitacao.status() == StatusSolicitacao.APROVADA;
        boolean prazoVencido = !solicitacao.dataHoraDesejada().isAfter(LocalDateTime.now());
        String modalId = "cancelar-" + solicitacao.id();
        return new CartaoMinhaSolicitacaoDTO(
            solicitacao.id(),
            solicitacao.origemExibicao() + " → " + solicitacao.destinoExibicao(),
            "%s · %d passageiro(s)".formatted(Formatos.dataHora(solicitacao.dataHoraDesejada()), solicitacao.qtdPassageiros()),
            solicitacao.motivoRecusa(),
            StatusSolicitacaoTela.rotulo(solicitacao.status()),
            StatusSolicitacaoTela.classe(solicitacao.status()) + " mb-2 d-inline-block",
            statusCancelavel && !prazoVencido,
            statusCancelavel && prazoVencido,
            modalId,
            "#" + modalId,
            "/minhas-solicitacoes/" + solicitacao.id() + "/cancelar"
        );
    }
}
