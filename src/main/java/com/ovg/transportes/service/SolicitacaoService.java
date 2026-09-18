package com.ovg.transportes.service;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.PaginaDTO;
import com.ovg.transportes.dto.ReprovarSolicitacaoRequestDTO;
import com.ovg.transportes.dto.SolicitacaoRequestDTO;
import com.ovg.transportes.dto.SolicitacaoResponseDTO;
import com.ovg.transportes.model.Cidade;
import com.ovg.transportes.model.LocalAdministrativo;
import com.ovg.transportes.model.Solicitacao;
import com.ovg.transportes.model.StatusSolicitacao;
import com.ovg.transportes.model.Usuario;
import com.ovg.transportes.repository.CidadeRepository;
import com.ovg.transportes.repository.LocalAdministrativoRepository;
import com.ovg.transportes.repository.SolicitacaoRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CidadeRepository cidadeRepository;
    private final LocalAdministrativoRepository localAdministrativoRepository;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    @Value("${transportes.limiar-urgencia-horas:2}")
    private int limiarUrgenciaHoras;

    public SolicitacaoService(
        SolicitacaoRepository solicitacaoRepository,
        CidadeRepository cidadeRepository,
        LocalAdministrativoRepository localAdministrativoRepository,
        UsuarioAutenticadoProvider usuarioAutenticadoProvider
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.cidadeRepository = cidadeRepository;
        this.localAdministrativoRepository = localAdministrativoRepository;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @Transactional
    public SolicitacaoResponseDTO criar(SolicitacaoRequestDTO requisicao) {
        Usuario solicitante = usuarioAutenticadoProvider.obterUsuarioLogado();
        PontoResolvido origem = resolverPonto("origem", requisicao.localOrigemId(), requisicao.cidadeOrigemId(), requisicao.descricaoOrigem());
        PontoResolvido destino = resolverPonto("destino", requisicao.localDestinoId(), requisicao.cidadeDestinoId(), requisicao.descricaoDestino());

        // regra decidida: sem restricao por departamento, vale para todos; abaixo do
        // limiar (padrao 2h) a solicitacao entra automaticamente como urgente.
        boolean urgente = Duration.between(LocalDateTime.now(), requisicao.dataHoraDesejada())
            .toHours() < limiarUrgenciaHoras;

        Solicitacao solicitacao = new Solicitacao(
            solicitante,
            origem.local(),
            origem.cidade(),
            origem.descricao(),
            destino.local(),
            destino.cidade(),
            destino.descricao(),
            requisicao.dataHoraDesejada(),
            requisicao.dataHoraRetornoDesejada(),
            requisicao.qtdPassageiros(),
            requisicao.telefoneContato(),
            requisicao.finalidade(),
            requisicao.observacoes(),
            urgente
        );

        return SolicitacaoResponseDTO.de(solicitacaoRepository.save(solicitacao));
    }

    public PaginaDTO<SolicitacaoResponseDTO> listarMinhas(Pageable pageable) {
        Usuario solicitante = usuarioAutenticadoProvider.obterUsuarioLogado();
        return PaginaDTO.de(
            solicitacaoRepository.findBySolicitanteIdOrderByIdDesc(solicitante.getId(), pageable),
            SolicitacaoResponseDTO::de
        );
    }

    @PreAuthorize("hasRole('TRANSPORTE')")
    public PaginaDTO<SolicitacaoResponseDTO> listarPorStatus(StatusSolicitacao status, Pageable pageable) {
        return PaginaDTO.de(
            solicitacaoRepository.findByStatusOrderByIdDesc(status, pageable),
            SolicitacaoResponseDTO::de
        );
    }

    @Transactional
    public SolicitacaoResponseDTO cancelar(Long id) {
        Solicitacao solicitacao = buscarPorId(id);
        Usuario usuarioLogado = usuarioAutenticadoProvider.obterUsuarioLogado();

        boolean ehDonoDaSolicitacao = solicitacao.getSolicitante().getId().equals(usuarioLogado.getId());
        boolean ehDoDepartamentoDeTransportes = usuarioLogado.temPerfil("TRANSPORTE");
        if (!ehDonoDaSolicitacao && !ehDoDepartamentoDeTransportes) {
            throw new NegocioException("Voce nao tem permissao para cancelar esta solicitacao");
        }

        solicitacao.cancelar();
        return SolicitacaoResponseDTO.de(solicitacao);
    }

    @Transactional
    @PreAuthorize("hasRole('TRANSPORTE')")
    public SolicitacaoResponseDTO reprovar(Long id, ReprovarSolicitacaoRequestDTO requisicao) {
        Solicitacao solicitacao = buscarPorId(id);
        solicitacao.reprovar(requisicao.motivoRecusa());
        return SolicitacaoResponseDTO.de(solicitacao);
    }

    public Solicitacao buscarPorId(Long id) {
        return solicitacaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao nao encontrada: " + id));
    }

    /**
     * Origem/destino aceitam ou uma unidade da OVG, ou uma cidade + descricao —
     * nunca as duas, nunca nenhuma. Resolve qualquer uma das formas para o
     * mesmo par (local opcional, cidade sempre presente, descricao opcional)
     * que a entidade Solicitacao guarda.
     */
    private PontoResolvido resolverPonto(String rotulo, Long localId, Long cidadeId, String descricao) {
        boolean informouLocal = localId != null;
        boolean informouCidade = cidadeId != null;

        if (informouLocal == informouCidade) {
            throw new NegocioException(
                "Informe uma unidade da OVG OU uma cidade para o %s, nunca as duas nem nenhuma".formatted(rotulo));
        }

        if (informouLocal) {
            LocalAdministrativo local = localAdministrativoRepository.findById(localId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade administrativa nao encontrada: " + localId));
            return new PontoResolvido(local, local.getCidade(), null);
        }

        Cidade cidade = buscarCidade(cidadeId);
        return new PontoResolvido(null, cidade, descricao);
    }

    private record PontoResolvido(LocalAdministrativo local, Cidade cidade, String descricao) {
    }

    private Cidade buscarCidade(Long id) {
        return cidadeRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cidade nao encontrada: " + id));
    }
}
