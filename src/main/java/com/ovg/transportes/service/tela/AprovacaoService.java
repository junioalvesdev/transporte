package com.ovg.transportes.service.tela;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.tela.ValidadorFormulario;
import com.ovg.transportes.dto.solicitacao.AprovarSolicitacaoRequestDTO;
import com.ovg.transportes.dto.solicitacao.ReprovarSolicitacaoRequestDTO;
import com.ovg.transportes.dto.solicitacao.SolicitacaoResponseDTO;
import com.ovg.transportes.dto.tela.formularios.AprovarReservaRequestDTO;
import com.ovg.transportes.dto.tela.formularios.FormAprovarDTO;
import com.ovg.transportes.dto.viagem.CriarViagemRequestDTO;
import com.ovg.transportes.dto.viagem.PontoRotaRequestDTO;
import com.ovg.transportes.dto.viagem.ViagemResponseDTO;
import com.ovg.transportes.model.StatusViagem;
import com.ovg.transportes.model.TipoPonto;
import com.ovg.transportes.service.SolicitacaoService;
import com.ovg.transportes.service.ViagemService;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Aprovar e reprovar uma solicitacao a partir das paginas (Lista de Reservas,
 * Solicitacoes pendentes e Calendario) — a mesma regra nas tres.
 *
 * Nao e @Transactional de proposito: ao aprovar, a viagem fica criada mesmo se
 * a troca de status logo depois falhar (cada chamada tem a sua transacao).
 */
@Service
public class AprovacaoService {

    private final SolicitacaoService solicitacaoService;
    private final ViagemService viagemService;
    private final OpcoesService opcoesService;
    private final ValidadorFormulario validador;

    public AprovacaoService(
        SolicitacaoService solicitacaoService,
        ViagemService viagemService,
        OpcoesService opcoesService,
        ValidadorFormulario validador
    ) {
        this.solicitacaoService = solicitacaoService;
        this.viagemService = viagemService;
        this.opcoesService = opcoesService;
        this.validador = validador;
    }

    // opcoes do formulario de aprovar; comStatusViagem = mostra o campo "Status da viagem"
    public FormAprovarDTO formulario(boolean comStatusViagem) {
        return new FormAprovarDTO(
            opcoesService.veiculosPorTipo(),
            opcoesService.motoristas(),
            comStatusViagem ? opcoesService.statusViagemParaAprovar() : null
        );
    }

    /** Aprova criando uma viagem simples origem -> destino; devolve a mensagem pra tela. */
    public String aprovar(Long id, AprovarReservaRequestDTO requisicao) {
        SolicitacaoResponseDTO solicitacao = solicitacaoService.detalhar(id);
        CriarViagemRequestDTO novaViagem = new CriarViagemRequestDTO(
            requisicao.veiculoId(),
            requisicao.motoristaId(),
            requisicao.saida(),
            requisicao.retorno(),
            List.of(
                new PontoRotaRequestDTO(solicitacao.localOrigemId(), solicitacao.cidadeOrigemId(), 1, TipoPonto.ORIGEM, null),
                new PontoRotaRequestDTO(solicitacao.localDestinoId(), solicitacao.cidadeDestinoId(), 2, TipoPonto.DESTINO, null)
            )
        );
        validador.validar(novaViagem);
        ViagemResponseDTO viagem = viagemService.aprovarSolicitacao(id, new AprovarSolicitacaoRequestDTO(
            null, novaViagem, solicitacao.cidadeOrigemId(), solicitacao.cidadeDestinoId()));

        // a aprovacao ja aconteceu (viagem criada) — se so a troca de status
        // falhar, isso nao pode aparecer como se a aprovacao inteira tivesse falhado
        String mensagem = "Solicitacao aprovada e viagem criada.";
        StatusViagem statusEscolhido = requisicao.statusViagem();
        if (statusEscolhido != null && statusEscolhido != viagem.status()) {
            try {
                viagemService.transicionarStatus(viagem.id(), statusEscolhido);
            } catch (NegocioException erroStatus) {
                mensagem += " A viagem ficou como %s (nao foi possivel mudar para %s: %s)."
                    .formatted(viagem.status(), statusEscolhido, erroStatus.getMessage());
            }
        }
        return mensagem;
    }

    public void reprovar(Long id, String motivoRecusa) {
        if (motivoRecusa == null || motivoRecusa.isBlank()) {
            throw new NegocioException("informe o motivo da recusa.");
        }
        solicitacaoService.reprovar(id, new ReprovarSolicitacaoRequestDTO(motivoRecusa.trim()));
    }
}
