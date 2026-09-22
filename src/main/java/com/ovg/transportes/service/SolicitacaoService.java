package com.ovg.transportes.service;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.AtualizarSolicitacaoRequestDTO;
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
import com.ovg.transportes.repository.ViagemParticipanteRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CidadeRepository cidadeRepository;
    private final LocalAdministrativoRepository localAdministrativoRepository;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final ViagemParticipanteRepository viagemParticipanteRepository;

    @Value("${transportes.limiar-urgencia-horas:2}")
    private int limiarUrgenciaHoras;

    public SolicitacaoService(
        SolicitacaoRepository solicitacaoRepository,
        CidadeRepository cidadeRepository,
        LocalAdministrativoRepository localAdministrativoRepository,
        UsuarioAutenticadoProvider usuarioAutenticadoProvider,
        ViagemParticipanteRepository viagemParticipanteRepository
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.cidadeRepository = cidadeRepository;
        this.localAdministrativoRepository = localAdministrativoRepository;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
        this.viagemParticipanteRepository = viagemParticipanteRepository;
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

    // Corrige uma solicitacao ainda pendente (ex.: horario/finalidade errados
    // ao criar) — mesma regra de quem pode mexer que o cancelar: o dono, ou
    // TRANSPORTE. So faz sentido enquanto nada foi decidido sobre ela ainda.
    @Transactional
    public SolicitacaoResponseDTO atualizar(Long id, AtualizarSolicitacaoRequestDTO requisicao) {
        Solicitacao solicitacao = buscarPorId(id);
        Usuario usuarioLogado = usuarioAutenticadoProvider.obterUsuarioLogado();

        boolean ehDonoDaSolicitacao = solicitacao.getSolicitante().getId().equals(usuarioLogado.getId());
        boolean ehDoDepartamentoDeTransportes = usuarioLogado.temPerfil("TRANSPORTE");
        if (!ehDonoDaSolicitacao && !ehDoDepartamentoDeTransportes) {
            throw new NegocioException("Voce nao tem permissao para editar esta solicitacao");
        }
        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new NegocioException("So e possivel editar uma solicitacao enquanto ela estiver pendente");
        }

        PontoResolvido origem = resolverPonto("origem", requisicao.localOrigemId(), requisicao.cidadeOrigemId(), requisicao.descricaoOrigem());
        PontoResolvido destino = resolverPonto("destino", requisicao.localDestinoId(), requisicao.cidadeDestinoId(), requisicao.descricaoDestino());

        solicitacao.atualizarDados(
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
            requisicao.observacoes()
        );

        return SolicitacaoResponseDTO.de(solicitacao);
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

    // "Lista de Reservas" (inspirada na tela equivalente do sistema legado):
    // so o mes atual, de qualquer status, com a placa do veiculo quando ja
    // existe uma viagem vinculada — a tabela real tem dezenas de milhares de
    // linhas migradas do legado, e a tela so agrupa/mostra um mes por vez, entao
    // nao faz sentido trazer tudo do banco so pra descartar o resto no front.
    @PreAuthorize("hasRole('TRANSPORTE')")
    public PaginaDTO<SolicitacaoResponseDTO> listarTodas(Pageable pageable) {
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime fimMes = inicioMes.plusMonths(1);
        Page<Solicitacao> pagina = solicitacaoRepository.findByDataHoraDesejadaBetweenOrderByIdDesc(inicioMes, fimMes, pageable);

        // placas de todas as solicitacoes da pagina numa unica consulta, em vez
        // de uma consulta por linha (N+1 que ficava mais lento quanto maior a
        // lista — bem perceptivel com centenas de solicitacoes no mes)
        List<Long> idsDaPagina = pagina.getContent().stream().map(Solicitacao::getId).toList();
        Map<Long, String> placaPorSolicitacaoId = idsDaPagina.isEmpty()
            ? Map.of()
            : viagemParticipanteRepository.findBySolicitacaoIdIn(idsDaPagina).stream()
                .collect(Collectors.toMap(
                    participante -> participante.getSolicitacao().getId(),
                    participante -> participante.getViagem().getVeiculo().getPlaca()
                ));

        return PaginaDTO.de(
            pagina,
            solicitacao -> SolicitacaoResponseDTO.de(solicitacao, placaPorSolicitacaoId.get(solicitacao.getId()))
        );
    }

    // Calendario: mostra solicitacoes que ainda NAO viraram viagem (pendente,
    // reprovada ou cancelada) no periodo visivel, pra nao sumir do calendario
    // ate que o Departamento de Transportes decida algo — aprovada/atendida ja
    // aparecem atraves da propria viagem vinculada.
    private static final List<StatusSolicitacao> STATUS_SEM_VIAGEM_VINCULADA =
        List.of(StatusSolicitacao.PENDENTE, StatusSolicitacao.REPROVADA, StatusSolicitacao.CANCELADA);

    public List<SolicitacaoResponseDTO> listarNoPeriodoParaCalendario(LocalDateTime inicio, LocalDateTime fim) {
        Usuario usuarioLogado = usuarioAutenticadoProvider.obterUsuarioLogado();
        List<Solicitacao> solicitacoes = usuarioLogado.temPerfil("TRANSPORTE")
            ? solicitacaoRepository.findByDataHoraDesejadaBetweenAndStatusInOrderByDataHoraDesejada(inicio, fim, STATUS_SEM_VIAGEM_VINCULADA)
            : solicitacaoRepository.findBySolicitanteIdAndDataHoraDesejadaBetweenAndStatusInOrderByDataHoraDesejada(
                usuarioLogado.getId(), inicio, fim, STATUS_SEM_VIAGEM_VINCULADA);
        return solicitacoes.stream().map(SolicitacaoResponseDTO::de).toList();
    }

    @Transactional
    public SolicitacaoResponseDTO cancelar(Long id) {
        Solicitacao solicitacao = buscarPorId(id);
        Usuario usuarioLogado = usuarioAutenticadoProvider.obterUsuarioLogado();

        // so quem fez o pedido pode cancela-lo — TRANSPORTE tem "Reprovar" (com
        // motivo) pra recusar uma solicitacao de outra pessoa, nao cancelar
        boolean ehDonoDaSolicitacao = solicitacao.getSolicitante().getId().equals(usuarioLogado.getId());
        if (!ehDonoDaSolicitacao) {
            throw new NegocioException("Somente quem fez a solicitacao pode cancela-la");
        }

        solicitacao.cancelar();

        // se ja tinha sido vinculada a uma viagem (aprovada e depois cancelada),
        // libera a vaga: cancela o participante e recalcula a ocupacao da
        // viagem, senao o lugar fica preso mesmo com a solicitacao cancelada
        viagemParticipanteRepository.findBySolicitacaoId(id).ifPresent(participante -> {
            participante.cancelar();
            participante.getViagem().recalcularStatusPorOcupacao();
        });

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
     * Origem/destino aceitam ou uma unidade administrativa, ou uma cidade + descricao —
     * nunca as duas, nunca nenhuma. Resolve qualquer uma das formas para o
     * mesmo par (local opcional, cidade sempre presente, descricao opcional)
     * que a entidade Solicitacao guarda.
     */
    private PontoResolvido resolverPonto(String rotulo, Long localId, Long cidadeId, String descricao) {
        boolean informouLocal = localId != null;
        boolean informouCidade = cidadeId != null;

        if (informouLocal == informouCidade) {
            throw new NegocioException(
                "Informe uma unidade administrativa OU uma cidade para o %s, nunca as duas nem nenhuma".formatted(rotulo));
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
