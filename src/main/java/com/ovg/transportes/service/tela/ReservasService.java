package com.ovg.transportes.service.tela;

import com.ovg.transportes.common.tela.Formatos;
import com.ovg.transportes.common.tela.Opcao;
import com.ovg.transportes.common.tela.StatusSolicitacaoTela;
import com.ovg.transportes.dto.solicitacao.SolicitacaoResponseDTO;
import com.ovg.transportes.dto.tela.formularios.FormNovaReservaDTO;
import com.ovg.transportes.dto.tela.formularios.NovaReservaRequestDTO;
import com.ovg.transportes.dto.tela.reservas.FiltrosReservasDTO;
import com.ovg.transportes.dto.tela.reservas.GrupoMesDTO;
import com.ovg.transportes.dto.tela.reservas.LinhaReservaDTO;
import com.ovg.transportes.dto.tela.reservas.ListaReservasDTO;
import com.ovg.transportes.model.StatusSolicitacao;
import com.ovg.transportes.service.SolicitacaoService;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Regras da pagina "Lista de Reservas": busca, filtra, agrupa por mes e
 * formata as reservas pra tabela. As acoes dos modais ficam nos services de
 * cada formulario (AprovacaoService, NovaReservaService, OrdensTrafegoService),
 * que o Calendario e as Solicitacoes pendentes tambem usam.
 */
@Service
public class ReservasService {

    // teto de linhas por tela: um periodo grande demais nos filtros nao traz
    // as dezenas de milhares de solicitacoes migradas do legado de uma vez
    private static final int LIMITE_RESERVAS = 1000;

    // ordem das opcoes no modal de filtros
    private static final List<StatusSolicitacao> ORDEM_FILTRO_STATUS = List.of(
        StatusSolicitacao.PENDENTE, StatusSolicitacao.APROVADA, StatusSolicitacao.ATENDIDA,
        StatusSolicitacao.REPROVADA, StatusSolicitacao.CANCELADA
    );

    private final SolicitacaoService solicitacaoService;
    private final AprovacaoService aprovacaoService;
    private final NovaReservaService novaReservaService;
    private final OrdensTrafegoService ordensTrafegoService;

    public ReservasService(
        SolicitacaoService solicitacaoService,
        AprovacaoService aprovacaoService,
        NovaReservaService novaReservaService,
        OrdensTrafegoService ordensTrafegoService
    ) {
        this.solicitacaoService = solicitacaoService;
        this.aprovacaoService = aprovacaoService;
        this.novaReservaService = novaReservaService;
        this.ordensTrafegoService = ordensTrafegoService;
    }

    /**
     * Tudo que a pagina mostra, ja pronto (textos, ids, opcoes marcadas).
     * novaReservaDigitada/erroNovaReserva: null na abertura normal; preenchidos
     * quando o envio de uma nova reserva deu erro e o modal volta aberto.
     */
    public ListaReservasDTO montarLista(FiltrosReservasDTO filtros, NovaReservaRequestDTO novaReservaDigitada, String erroNovaReserva) {
        Set<StatusSolicitacao> statusAtivos = statusAtivos(filtros);
        List<SolicitacaoResponseDTO> doPeriodo = solicitacaoService.listarNoPeriodo(
            inicioPeriodo(filtros), fimPeriodo(filtros), LIMITE_RESERVAS);
        List<SolicitacaoResponseDTO> reservas = doPeriodo.stream()
            .filter(reserva -> statusAtivos.contains(reserva.status()))
            .toList();

        String query = paraQuery(filtros);
        String urlLista = "/reservas" + (query.isEmpty() ? "" : "?" + query);
        FormNovaReservaDTO formNovaReserva = novaReservaService.formulario(
            "/reservas/nova", query, urlLista, novaReservaDigitada, erroNovaReserva);

        return new ListaReservasDTO(
            // tabela e barra de ferramentas
            agruparPorMes(reservas),
            "Filtros: " + quantidadeFiltrosAtivos(filtros),
            "%s · %d reserva(s)".formatted(descricaoPeriodo(filtros), reservas.size()),
            doPeriodo.size() >= LIMITE_RESERVAS,
            query,
            urlLista,
            formNovaReserva.aberto() ? "modal-open" : "",

            // modal de filtros
            ORDEM_FILTRO_STATUS.stream()
                .map(status -> new Opcao(status.name(), StatusSolicitacaoTela.rotulo(status), statusAtivos.contains(status)))
                .toList(),
            filtros.de() == null ? "" : filtros.de().toString(),
            filtros.ate() == null ? "" : filtros.ate().toString(),

            // formularios dos modais
            aprovacaoService.formulario(true),
            formNovaReserva,
            ordensTrafegoService.formulario()
        );
    }

    // o inverso de paraQuery(): remonta os filtros a partir do campo escondido
    // "filtros" que os formularios da tela enviam
    public FiltrosReservasDTO filtrosDaQuery(String query) {
        var parametros = UriComponentsBuilder.newInstance().query(query).build().getQueryParams();
        List<StatusSolicitacao> status = parametros.getOrDefault("status", List.of()).stream()
            .map(StatusSolicitacao::valueOf)
            .toList();
        LocalDate de = parametros.getFirst("de") == null ? null : LocalDate.parse(parametros.getFirst("de"));
        LocalDate ate = parametros.getFirst("ate") == null ? null : LocalDate.parse(parametros.getFirst("ate"));
        return new FiltrosReservasDTO(status, de, ate);
    }

    // =========================================================== filtros

    // nenhum status marcado = todos
    private static Set<StatusSolicitacao> statusAtivos(FiltrosReservasDTO filtros) {
        return filtros.status() == null || filtros.status().isEmpty()
            ? EnumSet.allOf(StatusSolicitacao.class)
            : EnumSet.copyOf(filtros.status());
    }

    // sem datas: so o mes atual (como a tela sempre foi). Com so uma das
    // datas, completa com um mes a partir/antes dela.
    private static LocalDateTime inicioPeriodo(FiltrosReservasDTO filtros) {
        if (filtros.de() != null) return filtros.de().atStartOfDay();
        if (filtros.ate() != null) return filtros.ate().minusMonths(1).plusDays(1).atStartOfDay();
        return YearMonth.now().atDay(1).atStartOfDay();
    }

    private static LocalDateTime fimPeriodo(FiltrosReservasDTO filtros) {
        if (filtros.ate() != null) return filtros.ate().plusDays(1).atStartOfDay();
        if (filtros.de() != null) return filtros.de().plusMonths(1).atStartOfDay();
        return YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
    }

    // "Setembro 2026" ou "01/09/2026 a 30/09/2026"
    private static String descricaoPeriodo(FiltrosReservasDTO filtros) {
        if (filtros.de() == null && filtros.ate() == null) {
            return Formatos.mesAno(YearMonth.now());
        }
        LocalDate inicio = inicioPeriodo(filtros).toLocalDate();
        LocalDate fim = fimPeriodo(filtros).toLocalDate().minusDays(1);
        return "%02d/%02d/%d a %02d/%02d/%d".formatted(
            inicio.getDayOfMonth(), inicio.getMonthValue(), inicio.getYear(),
            fim.getDayOfMonth(), fim.getMonthValue(), fim.getYear());
    }

    // quantos filtros estao diferentes do padrao (pro selo "Filtros: N")
    private static int quantidadeFiltrosAtivos(FiltrosReservasDTO filtros) {
        int contagem = StatusSolicitacao.values().length - statusAtivos(filtros).size();
        if (filtros.de() != null) contagem++;
        if (filtros.ate() != null) contagem++;
        return contagem;
    }

    // "status=PENDENTE&de=2026-09-01" — pra montar links e voltar pra mesma lista filtrada
    private static String paraQuery(FiltrosReservasDTO filtros) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        List<StatusSolicitacao> status = filtros.status();
        if (status != null && !status.isEmpty() && status.size() < StatusSolicitacao.values().length) {
            status.forEach(item -> builder.queryParam("status", item.name()));
        }
        if (filtros.de() != null) builder.queryParam("de", filtros.de());
        if (filtros.ate() != null) builder.queryParam("ate", filtros.ate());
        String query = builder.build().getQuery();
        return query == null ? "" : query;
    }

    // =========================================================== tabela

    // mais recente primeiro; dentro do mes, a mesma ordem do legado: urgente,
    // aguardando, disponivel/atendida, reprovada e cancelada (e a mais antiga antes)
    private static List<GrupoMesDTO> agruparPorMes(List<SolicitacaoResponseDTO> reservas) {
        Map<YearMonth, List<SolicitacaoResponseDTO>> porMes = reservas.stream()
            .collect(Collectors.groupingBy(
                (SolicitacaoResponseDTO reserva) -> YearMonth.from(reserva.dataHoraDesejada()),
                () -> new TreeMap<YearMonth, List<SolicitacaoResponseDTO>>(Comparator.reverseOrder()),
                Collectors.toList()));

        return porMes.entrySet().stream()
            .map(entrada -> {
                List<LinhaReservaDTO> linhas = entrada.getValue().stream()
                    .sorted(Comparator.comparingInt(ReservasService::prioridade)
                        .thenComparing(SolicitacaoResponseDTO::dataHoraDesejada))
                    .map(ReservasService::paraLinha)
                    .toList();
                String collapseId = "mes-" + entrada.getKey();
                return new GrupoMesDTO(
                    Formatos.mesAno(entrada.getKey()),
                    linhas.size() + " reserva(s)",
                    collapseId,
                    "#" + collapseId,
                    linhas);
            })
            .toList();
    }

    private static int prioridade(SolicitacaoResponseDTO reserva) {
        if (reserva.urgente()) return 0;
        return switch (reserva.status()) {
            case PENDENTE -> 1;
            case APROVADA, ATENDIDA -> 2;
            case REPROVADA -> 3;
            default -> 4;
        };
    }

    // uma reserva -> uma linha da tabela, com os textos ja formatados
    private static LinhaReservaDTO paraLinha(SolicitacaoResponseDTO reserva) {
        String modalId = "reserva-" + reserva.id();
        return new LinhaReservaDTO(
            reserva.id(),
            modalId,
            "#" + modalId,
            reserva.solicitanteNome(),
            Formatos.telefone(reserva.telefoneContato()),
            Formatos.textoOuTraco(reserva.finalidade()),
            reserva.urgente(),
            reserva.origemExibicao(),
            reserva.destinoExibicao(),
            Formatos.dataHora(reserva.dataHoraDesejada()),
            Formatos.dataHora(reserva.dataHoraRetornoDesejada()),
            Formatos.campoDataHora(reserva.dataHoraDesejada()),
            Formatos.campoDataHora(reserva.dataHoraRetornoDesejada()),
            StatusSolicitacaoTela.rotulo(reserva.status()),
            StatusSolicitacaoTela.classe(reserva.status()),
            reserva.qtdPassageiros(),
            reserva.veiculoPlaca() != null ? reserva.veiculoPlaca() : "N/A",
            Formatos.textoOuTraco(reserva.observacoes() != null ? reserva.observacoes() : reserva.finalidade()),
            reserva.motivoRecusa(),
            reserva.observacoes(),
            reserva.status() == StatusSolicitacao.PENDENTE,
            "/reservas/" + reserva.id() + "/aprovar",
            "/reservas/" + reserva.id() + "/reprovar"
        );
    }
}
