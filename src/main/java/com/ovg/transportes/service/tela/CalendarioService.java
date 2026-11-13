package com.ovg.transportes.service.tela;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.tela.Formatos;
import com.ovg.transportes.common.tela.Opcao;
import com.ovg.transportes.common.tela.StatusViagemTela;
import com.ovg.transportes.common.tela.ValidadorFormulario;
import com.ovg.transportes.dto.local.LocalAdministrativoResponseDTO;
import com.ovg.transportes.dto.motorista.MotoristaResponseDTO;
import com.ovg.transportes.dto.solicitacao.AtualizarSolicitacaoRequestDTO;
import com.ovg.transportes.dto.solicitacao.SolicitacaoResponseDTO;
import com.ovg.transportes.dto.tela.calendario.BuscaCaronaDTO;
import com.ovg.transportes.dto.tela.calendario.CartaoCaronaDTO;
import com.ovg.transportes.dto.tela.calendario.DiaCalendarioDTO;
import com.ovg.transportes.dto.tela.calendario.EditarSolicitacaoRequestDTO;
import com.ovg.transportes.dto.tela.calendario.EditarViagemRequestDTO;
import com.ovg.transportes.dto.tela.calendario.EventoDTO;
import com.ovg.transportes.dto.tela.calendario.FiltroStatusDTO;
import com.ovg.transportes.dto.tela.calendario.FiltrosCalendarioDTO;
import com.ovg.transportes.dto.tela.calendario.MiniDiaDTO;
import com.ovg.transportes.dto.tela.calendario.PaginaCalendarioDTO;
import com.ovg.transportes.dto.tela.calendario.SolicitacaoDetalheDTO;
import com.ovg.transportes.dto.tela.calendario.ViagemDetalheDTO;
import com.ovg.transportes.dto.tela.formularios.FormNovaReservaDTO;
import com.ovg.transportes.dto.tela.formularios.NovaReservaRequestDTO;
import com.ovg.transportes.dto.veiculo.VeiculoResponseDTO;
import com.ovg.transportes.dto.viagem.AtualizarViagemRequestDTO;
import com.ovg.transportes.dto.viagem.BuscaViagemRequestDTO;
import com.ovg.transportes.dto.viagem.SolicitarVagaRequestDTO;
import com.ovg.transportes.dto.viagem.ViagemDisponivelDTO;
import com.ovg.transportes.dto.viagem.ViagemResponseDTO;
import com.ovg.transportes.model.StatusSolicitacao;
import com.ovg.transportes.model.StatusViagem;
import com.ovg.transportes.model.Usuario;
import com.ovg.transportes.service.CidadeService;
import com.ovg.transportes.service.LocalAdministrativoService;
import com.ovg.transportes.service.MotoristaService;
import com.ovg.transportes.service.SolicitacaoService;
import com.ovg.transportes.service.UsuarioAutenticadoProvider;
import com.ovg.transportes.service.VeiculoService;
import com.ovg.transportes.service.ViagemDisponivelService;
import com.ovg.transportes.service.ViagemService;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Regras da pagina "Calendario" (renderizada no servidor): monta a grade do mes
 * ou da semana, os modais de detalhe e a busca de carona, tudo pronto pra
 * exibir; e executa as acoes do calendario (editar viagem, editar/cancelar
 * solicitacao, pegar vaga). Aprovar/reprovar e criar reserva ficam nos services
 * compartilhados (AprovacaoService, NovaReservaService).
 */
@Service
public class CalendarioService {

    private static final int EVENTOS_POR_DIA_NO_MES = 3;
    private static final List<String> DIAS_SEMANA = List.of("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab");
    private static final String[] MESES = {
        "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    private final ViagemService viagemService;
    private final SolicitacaoService solicitacaoService;
    private final ViagemDisponivelService viagemDisponivelService;
    private final VeiculoService veiculoService;
    private final MotoristaService motoristaService;
    private final LocalAdministrativoService localAdministrativoService;
    private final CidadeService cidadeService;
    private final AprovacaoService aprovacaoService;
    private final NovaReservaService novaReservaService;
    private final OrdensTrafegoService ordensTrafegoService;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final ValidadorFormulario validador;

    public CalendarioService(
        ViagemService viagemService,
        SolicitacaoService solicitacaoService,
        ViagemDisponivelService viagemDisponivelService,
        VeiculoService veiculoService,
        MotoristaService motoristaService,
        LocalAdministrativoService localAdministrativoService,
        CidadeService cidadeService,
        AprovacaoService aprovacaoService,
        NovaReservaService novaReservaService,
        OrdensTrafegoService ordensTrafegoService,
        UsuarioAutenticadoProvider usuarioAutenticadoProvider,
        ValidadorFormulario validador
    ) {
        this.viagemService = viagemService;
        this.solicitacaoService = solicitacaoService;
        this.viagemDisponivelService = viagemDisponivelService;
        this.veiculoService = veiculoService;
        this.motoristaService = motoristaService;
        this.localAdministrativoService = localAdministrativoService;
        this.cidadeService = cidadeService;
        this.aprovacaoService = aprovacaoService;
        this.novaReservaService = novaReservaService;
        this.ordensTrafegoService = ordensTrafegoService;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
        this.validador = validador;
    }

    // =========================================================== montar a pagina

    /**
     * Tudo que a pagina mostra. novaReservaDigitada/erroNovaReserva: null na
     * abertura normal; preenchidos quando o envio de uma nova reserva deu erro
     * e o modal volta aberto com o que foi digitado.
     */
    public PaginaCalendarioDTO montar(FiltrosCalendarioDTO filtros, NovaReservaRequestDTO novaReservaDigitada, String erroNovaReserva) {
        Usuario usuario = usuarioAutenticadoProvider.obterUsuarioLogado();
        boolean ehTransporte = usuario.temPerfil("TRANSPORTE");
        boolean visaoMes = !"semana".equals(filtros.modo());
        LocalDate data = filtros.data() != null ? filtros.data() : LocalDate.now();
        Set<StatusViagem> statusAtivos = statusAtivos(filtros);

        // periodo da grade: 6 semanas (42 dias) no mes, 7 dias na semana, sempre comecando no domingo
        LocalDate inicioGrade = visaoMes ? domingoDaSemana(data.withDayOfMonth(1)) : domingoDaSemana(data);
        int quantidadeDias = visaoMes ? 42 : 7;
        LocalDateTime inicio = inicioGrade.atStartOfDay();
        LocalDateTime fim = inicioGrade.plusDays(quantidadeDias).atStartOfDay();

        List<ViagemResponseDTO> viagens = viagemService.listarNoPeriodo(inicio, fim).stream()
            .filter(viagem -> statusAtivos.contains(viagem.status()))
            .toList();
        List<SolicitacaoResponseDTO> solicitacoes = solicitacaoService.listarNoPeriodoParaCalendario(inicio, fim);

        String query = paraQuery(filtros.modo(), data, filtros.status(), filtros.busca(), filtros.passageiros());
        String urlAtual = "/calendario" + (query.isEmpty() ? "" : "?" + query);
        BuscaCaronaDTO busca = montarBusca(filtros, data);
        FormNovaReservaDTO formNovaReserva = novaReservaService.formulario(
            "/calendario/nova", query, urlAtual, novaReservaDigitada, erroNovaReserva);
        boolean algumModalAberto = busca.aberta() || formNovaReserva.aberto();

        // opcoes de edicao de viagem (so TRANSPORTE edita)
        List<VeiculoResponseDTO> veiculos = ehTransporte ? veiculoService.listarAtivos(PageRequest.of(0, 1000)).conteudo() : List.of();
        List<MotoristaResponseDTO> motoristas = ehTransporte ? motoristaService.listarAtivos(PageRequest.of(0, 1000)).conteudo() : List.of();
        List<LocalAdministrativoResponseDTO> locais = localAdministrativoService.listar();

        LocalDate proximo = visaoMes ? data.plusMonths(1) : data.plusWeeks(1);
        LocalDate anterior = visaoMes ? data.minusMonths(1) : data.minusWeeks(1);
        String modo = visaoMes ? "mes" : "semana";

        return new PaginaCalendarioDTO(
            // cabecalho e navegacao
            visaoMes
                ? "%s de %d".formatted(MESES[data.getMonthValue() - 1], data.getYear())
                : "%s — %s".formatted(Formatos.data(inicioGrade), Formatos.data(inicioGrade.plusDays(6))),
            url(modo, anterior, filtros.status()),
            url(modo, proximo, filtros.status()),
            url(modo, LocalDate.now(), filtros.status()),
            url("mes", data, filtros.status()),
            url("semana", data, filtros.status()),
            visaoMes ? "btn btn-ovg-secundario ativo" : "btn btn-ovg-secundario",
            visaoMes ? "btn btn-ovg-secundario" : "btn btn-ovg-secundario ativo",
            urlAtual,
            query,
            ehTransporte,
            algumModalAberto ? "modal-open" : "",

            // grade
            visaoMes,
            DIAS_SEMANA,
            montarDias(inicioGrade, quantidadeDias, visaoMes, data, viagens, solicitacoes),

            // lateral
            "%s de %d".formatted(MESES[LocalDate.now().getMonthValue() - 1], LocalDate.now().getYear()),
            montarMiniCalendario(),
            Arrays.stream(StatusViagem.values())
                .map(status -> new FiltroStatusDTO(status.name(), StatusViagemTela.rotulo(status), statusAtivos.contains(status),
                    "filtro-status-bolinha " + StatusViagemTela.cor(status)))
                .toList(),
            modo,
            data.toString(),

            // modais de detalhe
            viagens.stream().map(viagem -> paraDetalhe(viagem, ehTransporte, veiculos, motoristas, query)).toList(),
            solicitacoes.stream().map(solicitacao -> paraDetalhe(solicitacao, ehTransporte, usuario.getLoginAd(), locais)).toList(),

            // modais compartilhados
            ehTransporte ? aprovacaoService.formulario(true) : null,
            formNovaReserva,
            ehTransporte ? ordensTrafegoService.formulario() : null,

            busca,
            cidadeService.listarNomesComUf()
        );
    }

    // o inverso de paraQuery(): remonta os filtros a partir do campo escondido
    // "filtros" que os formularios da pagina enviam
    public FiltrosCalendarioDTO filtrosDaQuery(String query) {
        var parametros = UriComponentsBuilder.newInstance().query(query).build().getQueryParams();
        List<StatusViagem> status = parametros.getOrDefault("status", List.of()).stream().map(StatusViagem::valueOf).toList();
        String data = parametros.getFirst("data");
        String busca = parametros.getFirst("busca");
        String passageiros = parametros.getFirst("passageiros");
        return new FiltrosCalendarioDTO(
            parametros.getFirst("modo"),
            data == null ? null : LocalDate.parse(data),
            status,
            busca == null ? null : LocalDate.parse(busca),
            passageiros == null ? null : Integer.valueOf(passageiros));
    }

    // =========================================================== acoes

    /** TRANSPORTE corrige veiculo, motorista, horarios, capacidade e status de uma viagem. */
    public void editarViagem(Long id, EditarViagemRequestDTO requisicao) {
        AtualizarViagemRequestDTO atualizacao = new AtualizarViagemRequestDTO(
            requisicao.veiculoId(), requisicao.motoristaId(), requisicao.saida(), requisicao.retorno(), requisicao.capacidade());
        validador.validar(atualizacao);
        viagemService.atualizarDetalhes(id, atualizacao);
        // status e uma transicao a parte (com regras de quem pode ir pra onde):
        // so dispara se a pessoa realmente trocou o valor
        if (requisicao.status() != null && requisicao.status() != requisicao.statusAtual()) {
            viagemService.transicionarStatus(id, requisicao.status());
        }
    }

    /** Quem pediu corrige os dados da propria solicitacao enquanto ela esta pendente. */
    public void editarSolicitacao(Long id, EditarSolicitacaoRequestDTO form) {
        Long cidadeOrigemId = form.localOrigemId() == null ? solicitacaoService.buscarIdCidadePorNomeComUf(form.cidadeOrigem()) : null;
        Long cidadeDestinoId = form.localDestinoId() == null ? solicitacaoService.buscarIdCidadePorNomeComUf(form.cidadeDestino()) : null;
        AtualizarSolicitacaoRequestDTO atualizacao = new AtualizarSolicitacaoRequestDTO(
            form.localOrigemId(), cidadeOrigemId, form.localOrigemId() == null ? vazioComoNulo(form.descricaoOrigem()) : null,
            form.localDestinoId(), cidadeDestinoId, form.localDestinoId() == null ? vazioComoNulo(form.descricaoDestino()) : null,
            form.saida(), form.retorno(), form.qtdPassageiros(), form.telefone(), form.finalidade(), vazioComoNulo(form.observacoes()));
        validador.validar(atualizacao);
        solicitacaoService.atualizar(id, atualizacao);
    }

    // cancelar a propria solicitacao, a propria vaga numa carona ou a participacao numa viagem
    public void cancelarSolicitacao(Long id) {
        solicitacaoService.cancelar(id);
    }

    /** Pega vaga numa viagem aberta pra aproveitamento (busca de carona). */
    public void solicitarVaga(Long viagemId, Integer qtdPassageiros, Long cidadeEmbarqueId, Long cidadeDesembarqueId) {
        SolicitarVagaRequestDTO requisicao = new SolicitarVagaRequestDTO(qtdPassageiros, cidadeEmbarqueId, cidadeDesembarqueId, null, null);
        validador.validar(requisicao);
        viagemService.solicitarVaga(viagemId, requisicao);
    }

    // =========================================================== grade

    private static LocalDate domingoDaSemana(LocalDate dia) {
        return dia.minusDays(dia.getDayOfWeek().getValue() % 7);
    }

    private List<DiaCalendarioDTO> montarDias(
        LocalDate inicioGrade, int quantidade, boolean visaoMes, LocalDate referencia,
        List<ViagemResponseDTO> viagens, List<SolicitacaoResponseDTO> solicitacoes
    ) {
        LocalDate hoje = LocalDate.now();
        List<DiaCalendarioDTO> dias = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            LocalDate dia = inicioGrade.plusDays(i);
            List<EventoDTO> eventos = eventosDoDia(dia, viagens, solicitacoes);
            // no mes cabem ate 3 pilulas por dia; na semana, todas
            List<EventoDTO> visiveis = visaoMes && eventos.size() > EVENTOS_POR_DIA_NO_MES
                ? eventos.subList(0, EVENTOS_POR_DIA_NO_MES)
                : eventos;
            int restantes = eventos.size() - visiveis.size();

            String classe = visaoMes
                ? "mes-celula" + (dia.getMonthValue() != referencia.getMonthValue() ? " fora-do-mes" : "") + (dia.equals(hoje) ? " hoje" : "")
                : "semana-cabecalho" + (dia.equals(hoje) ? " hoje" : "");
            String modalDiaId = "dia-" + dia;
            dias.add(new DiaCalendarioDTO(
                dia.getDayOfMonth(),
                classe,
                visiveis,
                eventos,
                restantes > 0 ? "+%d mais".formatted(restantes) : null,
                modalDiaId,
                "#" + modalDiaId,
                "Viagens de " + Formatos.data(dia)
            ));
        }
        return dias;
    }

    // viagens e solicitacoes do dia, na ordem do horario
    private static List<EventoDTO> eventosDoDia(LocalDate dia, List<ViagemResponseDTO> viagens, List<SolicitacaoResponseDTO> solicitacoes) {
        record Item(LocalDateTime horario, EventoDTO evento) {
        }
        Stream<Item> daViagem = viagens.stream()
            .filter(viagem -> viagem.dataHoraSaida().toLocalDate().equals(dia))
            .map(viagem -> new Item(viagem.dataHoraSaida(), paraEvento(viagem)));
        Stream<Item> daSolicitacao = solicitacoes.stream()
            .filter(solicitacao -> solicitacao.dataHoraDesejada().toLocalDate().equals(dia))
            .map(solicitacao -> new Item(solicitacao.dataHoraDesejada(), paraEvento(solicitacao)));
        return Stream.concat(daViagem, daSolicitacao)
            .sorted(Comparator.comparing(Item::horario))
            .map(Item::evento)
            .toList();
    }

    private static EventoDTO paraEvento(ViagemResponseDTO viagem) {
        String hora = Formatos.hora(viagem.dataHoraSaida());
        String finalidade = viagem.finalidade() != null ? viagem.finalidade() : "Sem finalidade informada";
        String urgente = viagem.urgente() ? " · URGENTE" : "";
        String ocupacao = "%s · %s (%d/%d)".formatted(viagem.veiculoPlaca(), viagem.motoristaNome(), viagem.vagasOcupadas(), viagem.capacidadeTotal());
        return new EventoDTO(
            hora + " " + finalidade,
            hora + " · " + ocupacao + urgente,
            finalidade + " — " + ocupacao + urgente,
            "evento-pilula " + StatusViagemTela.cor(viagem.status()) + (viagem.urgente() ? " urgente" : ""),
            "#viagem-" + viagem.id()
        );
    }

    private static EventoDTO paraEvento(SolicitacaoResponseDTO solicitacao) {
        String hora = Formatos.hora(solicitacao.dataHoraDesejada());
        String finalidade = solicitacao.finalidade() != null ? solicitacao.finalidade() : "Sem finalidade informada";
        String rotulo = StatusViagemTela.rotulo(solicitacao.status());
        String texto = "%s [%s] %s".formatted(hora, rotulo, finalidade);
        return new EventoDTO(
            texto,
            texto,
            "%s — solicitacao %s%s".formatted(finalidade, rotulo.toLowerCase(), solicitacao.urgente() ? " · URGENTE" : ""),
            "evento-pilula " + StatusViagemTela.cor(solicitacao.status()) + (solicitacao.urgente() ? " urgente" : ""),
            "#solicitacao-" + solicitacao.id()
        );
    }

    // mini calendario da lateral: sempre o mes atual de verdade, com hoje marcado
    private static List<MiniDiaDTO> montarMiniCalendario() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = domingoDaSemana(hoje.withDayOfMonth(1));
        List<MiniDiaDTO> dias = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            LocalDate dia = inicio.plusDays(i);
            String classe = "mini-dia" + (dia.getMonthValue() != hoje.getMonthValue() ? " fora-do-mes" : "") + (dia.equals(hoje) ? " hoje" : "");
            dias.add(new MiniDiaDTO(dia.getDayOfMonth(), classe));
        }
        return dias;
    }

    // =========================================================== modais de detalhe

    private static ViagemDetalheDTO paraDetalhe(
        ViagemResponseDTO viagem, boolean ehTransporte,
        List<VeiculoResponseDTO> veiculos, List<MotoristaResponseDTO> motoristas, String query
    ) {
        boolean passada = viagem.dataHoraSaida().toLocalDate().isBefore(LocalDate.now());
        boolean podeEditar = ehTransporte && !passada;
        // "Solicitado por" so com quem pediu a viagem; quem entrou depois pela
        // busca de caronas aparece separado, na ordem em que foram entrando
        List<String> solicitantes = viagem.participantes().stream()
            .filter(participante -> !participante.viaCarona())
            .map(ViagemResponseDTO.ParticipanteDTO::nome)
            .distinct()
            .toList();
        List<String> caronas = viagem.participantes().stream()
            .filter(ViagemResponseDTO.ParticipanteDTO::viaCarona)
            .map(ViagemResponseDTO.ParticipanteDTO::nome)
            .toList();
        String ocupacao = viagem.vagasOcupadas() + "/" + viagem.capacidadeTotal();

        return new ViagemDetalheDTO(
            "viagem-" + viagem.id(),
            solicitantes.isEmpty() ? "-" : String.join(", ", solicitantes),
            caronas,
            viagem.telefonesContato().isEmpty() ? null
                : String.join(", ", viagem.telefonesContato().stream().map(Formatos::telefone).toList()),
            viagem.finalidade(),
            Formatos.textoOuTraco(viagem.origemExibicao()),
            Formatos.textoOuTraco(viagem.destinoExibicao()),
            Formatos.dataHora(viagem.dataHoraSaida()),
            Formatos.dataHora(viagem.dataHoraChegadaEstimada()),
            ocupacao,
            viagem.veiculoPlaca(),
            viagem.motoristaNome(),
            StatusViagemTela.rotulo(viagem.status()),
            "evento-pilula " + StatusViagemTela.cor(viagem.status()),
            viagem.observacoes().isEmpty() ? null : String.join(" | ", viagem.observacoes()),
            passada,
            podeEditar,
            !ehTransporte && !passada && viagem.minhaSolicitacaoId() != null,
            "/calendario/viagens/" + viagem.id(),
            "editar-viagem-" + viagem.id(),
            viagem.status().name(),
            podeEditar ? veiculos.stream()
                .map(veiculo -> new Opcao(String.valueOf(veiculo.id()), "%s - %s (%d lugares)".formatted(veiculo.placa(), veiculo.modelo(), veiculo.capacidade()),
                    veiculo.id().equals(viagem.veiculoId())))
                .toList() : List.of(),
            podeEditar ? motoristas.stream()
                .map(motorista -> new Opcao(String.valueOf(motorista.id()), motorista.nome(), motorista.id().equals(viagem.motoristaId())))
                .toList() : List.of(),
            Formatos.campoDataHora(viagem.dataHoraSaida()),
            Formatos.campoDataHora(viagem.dataHoraChegadaEstimada()),
            viagem.capacidadeTotal(),
            Math.max(viagem.vagasOcupadas(), 1),
            "Capacidade (ocupacao atual: " + ocupacao + ")",
            Arrays.stream(StatusViagem.values())
                .map(status -> new Opcao(status.name(), StatusViagemTela.rotulo(status), status == viagem.status()))
                .toList(),
            viagem.minhaSolicitacaoId() == null ? null : "/calendario/solicitacoes/" + viagem.minhaSolicitacaoId() + "/cancelar"
        );
    }

    private SolicitacaoDetalheDTO paraDetalhe(
        SolicitacaoResponseDTO solicitacao, boolean ehTransporte, String loginUsuario, List<LocalAdministrativoResponseDTO> locais
    ) {
        boolean pendente = solicitacao.status() == StatusSolicitacao.PENDENTE;
        boolean souODono = loginUsuario != null && loginUsuario.equals(solicitacao.solicitanteLogin());
        boolean podeEditar = pendente && souODono && !ehTransporte;
        String base = "/calendario/solicitacoes/" + solicitacao.id();

        return new SolicitacaoDetalheDTO(
            "solicitacao-" + solicitacao.id(),
            Formatos.textoOuTraco(solicitacao.solicitanteNome()),
            solicitacao.telefoneContato() == null || solicitacao.telefoneContato().isBlank() ? null : Formatos.telefone(solicitacao.telefoneContato()),
            solicitacao.finalidade(),
            Formatos.textoOuTraco(solicitacao.origemExibicao()),
            Formatos.textoOuTraco(solicitacao.destinoExibicao()),
            Formatos.dataHora(solicitacao.dataHoraDesejada()),
            Formatos.dataHora(solicitacao.dataHoraRetornoDesejada()),
            solicitacao.qtdPassageiros(),
            StatusViagemTela.rotulo(solicitacao.status()),
            "evento-pilula " + StatusViagemTela.cor(solicitacao.status()),
            solicitacao.observacoes(),
            solicitacao.motivoRecusa(),
            pendente && ehTransporte,
            podeEditar,
            base + "/aprovar",
            base + "/reprovar",
            Formatos.campoDataHora(solicitacao.dataHoraDesejada()),
            Formatos.campoDataHora(solicitacao.dataHoraRetornoDesejada()),
            base,
            "editar-solicitacao-" + solicitacao.id(),
            base + "/cancelar",
            solicitacao.finalidade(),
            podeEditar ? opcoesDeLocal(locais, solicitacao.localOrigemId()) : List.of(),
            podeEditar && solicitacao.localOrigemId() == null ? cidadeService.nomeComUf(solicitacao.cidadeOrigemId()) : null,
            solicitacao.descricaoOrigem(),
            podeEditar ? opcoesDeLocal(locais, solicitacao.localDestinoId()) : List.of(),
            podeEditar && solicitacao.localDestinoId() == null ? cidadeService.nomeComUf(solicitacao.cidadeDestinoId()) : null,
            solicitacao.descricaoDestino(),
            solicitacao.telefoneContato(),
            solicitacao.observacoes()
        );
    }

    // unidades da OVG + "Outro local" no fim, com a opcao atual marcada
    private static List<Opcao> opcoesDeLocal(List<LocalAdministrativoResponseDTO> locais, Long localAtual) {
        List<Opcao> opcoes = new ArrayList<>();
        locais.forEach(local -> opcoes.add(new Opcao(String.valueOf(local.id()), local.nome(), local.id().equals(localAtual))));
        opcoes.add(new Opcao("", "Outro local (informe a cidade)", localAtual == null));
        return opcoes;
    }

    // =========================================================== busca de carona

    private BuscaCaronaDTO montarBusca(FiltrosCalendarioDTO filtros, LocalDate dataCalendario) {
        LocalDate hoje = LocalDate.now();
        int passageiros = filtros.passageiros() != null ? filtros.passageiros() : 1;
        boolean aberta = filtros.busca() != null;
        String urlFechar = "/calendario?" + paraQuery(filtros.modo(), dataCalendario, filtros.status(), null, null);

        List<CartaoCaronaDTO> cartoes = List.of();
        String erro = null;
        if (aberta) {
            try {
                BuscaViagemRequestDTO requisicao = new BuscaViagemRequestDTO(filtros.busca(), passageiros);
                validador.validar(requisicao);
                cartoes = viagemDisponivelService.buscar(requisicao).stream().map(CalendarioService::paraCartao).toList();
            } catch (NegocioException invalida) {
                erro = invalida.getMessage();
            }
        }

        return new BuscaCaronaDTO(
            aberta,
            aberta ? "modal fade show d-block" : "modal fade",
            aberta ? filtros.busca().toString() : hoje.toString(),
            hoje.toString(),
            passageiros,
            aberta ? "Viagens disponiveis em " + Formatos.data(filtros.busca()) : "Viagens disponiveis",
            erro,
            cartoes,
            aberta && erro == null && cartoes.isEmpty(),
            urlFechar
        );
    }

    private static CartaoCaronaDTO paraCartao(ViagemDisponivelDTO viagem) {
        boolean jaSolicitei = viagem.minhaSolicitacaoId() != null;
        String chegada = viagem.dataHoraChegadaEstimada() != null
            ? " · Chegada estimada: " + Formatos.hora(viagem.dataHoraChegadaEstimada())
            : "";
        return new CartaoCaronaDTO(
            viagem.origem().toUpperCase() + " → " + viagem.destino().toUpperCase(),
            Formatos.data(viagem.dataHoraSaida().toLocalDate()),
            "Saida: " + Formatos.hora(viagem.dataHoraSaida()) + chegada,
            viagem.tipoVeiculo(),
            Formatos.iconeTipoVeiculo(viagem.tipoVeiculo()) + " me-1",
            viagem.vagasDisponiveis() + " lugares disponiveis",
            "badge badge-vagas mb-2 d-inline-block" + (viagem.vagasDisponiveis() <= 3 ? " baixa" : ""),
            "cartao cartao-viagem d-flex justify-content-between align-items-center flex-wrap gap-3"
                + (jaSolicitei ? " cartao-viagem-ja-solicitada" : ""),
            jaSolicitei,
            "/calendario/viagens/" + viagem.viagemId() + "/vaga",
            viagem.cidadeOrigemId(),
            viagem.cidadeDestinoId(),
            jaSolicitei ? "/calendario/solicitacoes/" + viagem.minhaSolicitacaoId() + "/cancelar" : null
        );
    }

    // =========================================================== links e filtros

    // nenhum status marcado = todos
    private static Set<StatusViagem> statusAtivos(FiltrosCalendarioDTO filtros) {
        return filtros.status() == null || filtros.status().isEmpty()
            ? EnumSet.allOf(StatusViagem.class)
            : EnumSet.copyOf(filtros.status());
    }

    private static String url(String modo, LocalDate data, List<StatusViagem> status) {
        return "/calendario?" + paraQuery(modo, data, status, null, null);
    }

    // "modo=semana&data=2026-09-22&status=PLANEJADA&busca=...&passageiros=..."
    private static String paraQuery(String modo, LocalDate data, List<StatusViagem> status, LocalDate busca, Integer passageiros) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.queryParam("modo", "semana".equals(modo) ? "semana" : "mes");
        if (data != null) builder.queryParam("data", data);
        if (status != null && !status.isEmpty() && status.size() < StatusViagem.values().length) {
            status.forEach(item -> builder.queryParam("status", item.name()));
        }
        if (busca != null) builder.queryParam("busca", busca);
        if (busca != null && passageiros != null) builder.queryParam("passageiros", passageiros);
        String query = builder.build().getQuery();
        return query == null ? "" : query;
    }

    private static String vazioComoNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
