package com.ovg.transportes.service;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.AprovarSolicitacaoRequestDTO;
import com.ovg.transportes.dto.AtualizarViagemRequestDTO;
import com.ovg.transportes.dto.CriarViagemRequestDTO;
import com.ovg.transportes.dto.PontoRotaRequestDTO;
import com.ovg.transportes.dto.SolicitarVagaRequestDTO;
import com.ovg.transportes.dto.ViagemResponseDTO;
import com.ovg.transportes.model.Cidade;
import com.ovg.transportes.model.LocalAdministrativo;
import com.ovg.transportes.model.Motorista;
import com.ovg.transportes.model.PontoRota;
import com.ovg.transportes.model.Rota;
import com.ovg.transportes.model.Solicitacao;
import com.ovg.transportes.model.StatusParticipante;
import com.ovg.transportes.model.StatusViagem;
import com.ovg.transportes.model.Usuario;
import com.ovg.transportes.model.Veiculo;
import com.ovg.transportes.model.Viagem;
import com.ovg.transportes.model.ViagemParticipante;
import com.ovg.transportes.repository.CidadeRepository;
import com.ovg.transportes.repository.LocalAdministrativoRepository;
import com.ovg.transportes.repository.MotoristaRepository;
import com.ovg.transportes.repository.RotaRepository;
import com.ovg.transportes.repository.VeiculoRepository;
import com.ovg.transportes.repository.ViagemParticipanteRepository;
import com.ovg.transportes.repository.ViagemRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orquestra o nucleo do sistema: transformar uma Solicitacao numa Viagem (nova ou
 * existente) e controlar quantas pessoas cabem em cada viagem — o mecanismo de
 * "aproveitamento" descrito no documento de arquitetura.
 */
@Service
@Transactional(readOnly = true)
public class ViagemService {

    private final ViagemRepository viagemRepository;
    private final VeiculoRepository veiculoRepository;
    private final MotoristaRepository motoristaRepository;
    private final CidadeRepository cidadeRepository;
    private final LocalAdministrativoRepository localAdministrativoRepository;
    private final ViagemParticipanteRepository viagemParticipanteRepository;
    private final RotaRepository rotaRepository;
    private final SolicitacaoService solicitacaoService;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public ViagemService(
        ViagemRepository viagemRepository,
        VeiculoRepository veiculoRepository,
        MotoristaRepository motoristaRepository,
        CidadeRepository cidadeRepository,
        LocalAdministrativoRepository localAdministrativoRepository,
        ViagemParticipanteRepository viagemParticipanteRepository,
        RotaRepository rotaRepository,
        SolicitacaoService solicitacaoService,
        UsuarioAutenticadoProvider usuarioAutenticadoProvider
    ) {
        this.viagemRepository = viagemRepository;
        this.veiculoRepository = veiculoRepository;
        this.motoristaRepository = motoristaRepository;
        this.cidadeRepository = cidadeRepository;
        this.localAdministrativoRepository = localAdministrativoRepository;
        this.viagemParticipanteRepository = viagemParticipanteRepository;
        this.rotaRepository = rotaRepository;
        this.solicitacaoService = solicitacaoService;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @Transactional
    @PreAuthorize("hasRole('TRANSPORTE')")
    public ViagemResponseDTO aprovarSolicitacao(Long solicitacaoId, AprovarSolicitacaoRequestDTO requisicao) {
        Solicitacao solicitacao = solicitacaoService.buscarPorId(solicitacaoId);
        Cidade cidadeEmbarque = buscarCidade(requisicao.cidadeEmbarqueId());
        Cidade cidadeDesembarque = buscarCidade(requisicao.cidadeDesembarqueId());

        Viagem viagem = requisicao.viagemExistenteId() != null
            ? buscarPorId(requisicao.viagemExistenteId())
            : criarViagemComRota(requisicao.novaViagem());

        vincularParticipante(viagem, solicitacao, solicitacao.getQtdPassageiros(), cidadeEmbarque, cidadeDesembarque);
        solicitacao.aprovar();

        return ViagemResponseDTO.de(viagem);
    }

    @Transactional
    public ViagemResponseDTO solicitarVaga(Long viagemId, SolicitarVagaRequestDTO requisicao) {
        Viagem viagem = buscarPorId(viagemId);
        if (viagem.getStatus() != StatusViagem.ABERTA_PARA_APROVEITAMENTO) {
            throw new NegocioException("Esta viagem nao esta aberta para aproveitamento");
        }
        if (!viagem.temVagaPara(requisicao.qtdPassageiros())) {
            throw new NegocioException("Vagas insuficientes: restam %d, solicitado %d"
                .formatted(viagem.vagasDisponiveis(), requisicao.qtdPassageiros()));
        }

        Usuario solicitante = usuarioAutenticadoProvider.obterUsuarioLogado();
        Cidade cidadeEmbarque = buscarCidade(requisicao.cidadeEmbarqueId());
        Cidade cidadeDesembarque = buscarCidade(requisicao.cidadeDesembarqueId());

        // aproveitamento e autosservico: a viagem ja foi aberta deliberadamente pelo
        // Departamento de Transportes, entao a solicitacao nasce direto como APROVADA.
        Solicitacao solicitacao = new Solicitacao(
            solicitante,
            null,
            cidadeEmbarque,
            null,
            null,
            cidadeDesembarque,
            null,
            viagem.getDataHoraSaida(),
            null,
            requisicao.qtdPassageiros(),
            solicitante.getTelefone(),
            requisicao.finalidade(),
            requisicao.observacoes(),
            false
        );
        solicitacao.aprovar();

        vincularParticipante(viagem, solicitacao, requisicao.qtdPassageiros(), cidadeEmbarque, cidadeDesembarque);
        return ViagemResponseDTO.de(viagem);
    }

    @Transactional
    @PreAuthorize("hasRole('TRANSPORTE')")
    public ViagemResponseDTO criarViagemAutonoma(CriarViagemRequestDTO requisicao) {
        return ViagemResponseDTO.de(criarViagemComRota(requisicao));
    }

    @Transactional
    @PreAuthorize("hasRole('TRANSPORTE')")
    public ViagemResponseDTO transicionarStatus(Long id, StatusViagem novoStatus) {
        Viagem viagem = buscarPorId(id);
        viagem.transicionarPara(novoStatus);
        return ViagemResponseDTO.de(viagem);
    }

    @Transactional
    @PreAuthorize("hasRole('TRANSPORTE')")
    public ViagemResponseDTO atualizarDetalhes(Long id, AtualizarViagemRequestDTO requisicao) {
        Viagem viagem = buscarPorId(id);
        Veiculo veiculo = veiculoRepository.findById(requisicao.veiculoId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado: " + requisicao.veiculoId()));
        Motorista motorista = motoristaRepository.findById(requisicao.motoristaId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista nao encontrado: " + requisicao.motoristaId()));

        viagem.atualizarDetalhes(veiculo, motorista, requisicao.dataHoraSaida(), requisicao.dataHoraChegadaEstimada(), requisicao.capacidadeTotal());
        return ViagemResponseDTO.de(viagem);
    }

    public List<ViagemResponseDTO> listarNoPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Long usuarioLogadoId = usuarioAutenticadoProvider.obterUsuarioLogado().getId();
        return viagemRepository.findByDataHoraSaidaBetween(inicio, fim).stream()
            .map(viagem -> ViagemResponseDTO.de(viagem, minhaSolicitacaoNaViagem(viagem, usuarioLogadoId)))
            .toList();
    }

    // solicitacao do usuario logado dentro desta viagem, se houver — usado pelo
    // calendario pra saber se mostra o botao "Cancelar" (so cancela a propria
    // participacao, nunca a viagem toda) pra quem nao e do Departamento de Transportes
    private Long minhaSolicitacaoNaViagem(Viagem viagem, Long usuarioLogadoId) {
        return viagem.getParticipantes().stream()
            .filter(participante -> participante.getStatus() != StatusParticipante.CANCELADO)
            .filter(participante -> participante.getSolicitacao().getSolicitante().getId().equals(usuarioLogadoId))
            .map(participante -> participante.getSolicitacao().getId())
            .findFirst()
            .orElse(null);
    }

    private Viagem criarViagemComRota(CriarViagemRequestDTO requisicao) {
        Veiculo veiculo = veiculoRepository.findById(requisicao.veiculoId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado: " + requisicao.veiculoId()));
        Motorista motorista = motoristaRepository.findById(requisicao.motoristaId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista nao encontrado: " + requisicao.motoristaId()));

        if (viagemRepository.existeConflitoDeAgendaParaVeiculo(veiculo.getId(), requisicao.dataHoraSaida(), requisicao.dataHoraChegadaEstimada())) {
            throw new NegocioException("Este veiculo ja tem outra viagem no mesmo horario");
        }
        if (viagemRepository.existeConflitoDeAgendaParaMotorista(motorista.getId(), requisicao.dataHoraSaida(), requisicao.dataHoraChegadaEstimada())) {
            throw new NegocioException("Este motorista ja tem outra viagem no mesmo horario");
        }

        Viagem viagem = new Viagem(veiculo, motorista, requisicao.dataHoraSaida(), requisicao.dataHoraChegadaEstimada());
        viagem.transicionarPara(StatusViagem.ABERTA_PARA_APROVEITAMENTO);
        viagem = viagemRepository.save(viagem);

        Rota rota = new Rota(viagem);
        // "sem for": cada ponto do request vira um PontoRota na mesma ordem, sem
        // precisar de um indice manual controlado a mao.
        requisicao.pontos().stream()
            .map(paraPontoRota(rota))
            .forEach(rota::adicionarPonto);
        // CascadeType.ALL em Rota.pontos persiste os PontoRota junto, sem precisar
        // de um PontoRotaRepository.save() para cada um.
        rotaRepository.save(rota);

        return viagem;
    }

    private java.util.function.Function<PontoRotaRequestDTO, PontoRota> paraPontoRota(Rota rota) {
        return dto -> new PontoRota(
            rota,
            buscarCidade(dto.cidadeId()),
            dto.localId() != null ? buscarLocal(dto.localId()) : null,
            dto.ordem(),
            dto.tipoPonto(),
            dto.horarioEstimado()
        );
    }

    private void vincularParticipante(Viagem viagem, Solicitacao solicitacao, int qtdPassageiros, Cidade cidadeEmbarque, Cidade cidadeDesembarque) {
        if (!viagem.temVagaPara(qtdPassageiros)) {
            throw new NegocioException("Vagas insuficientes na viagem selecionada");
        }
        ViagemParticipante participante = new ViagemParticipante(viagem, solicitacao, qtdPassageiros, cidadeEmbarque, cidadeDesembarque);
        viagemParticipanteRepository.save(participante);
        viagem.recalcularStatusPorOcupacao();
    }

    Viagem buscarPorId(Long id) {
        return viagemRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Viagem nao encontrada: " + id));
    }

    private Cidade buscarCidade(Long id) {
        return cidadeRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cidade nao encontrada: " + id));
    }

    private LocalAdministrativo buscarLocal(Long id) {
        return localAdministrativoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Local administrativo nao encontrado: " + id));
    }
}
