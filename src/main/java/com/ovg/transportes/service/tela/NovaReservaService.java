package com.ovg.transportes.service.tela;

import com.ovg.transportes.common.tela.Formatos;
import com.ovg.transportes.common.tela.Opcao;
import com.ovg.transportes.common.tela.ValidadorFormulario;
import com.ovg.transportes.dto.local.LocalAdministrativoResponseDTO;
import com.ovg.transportes.dto.solicitacao.SolicitacaoRequestDTO;
import com.ovg.transportes.dto.tela.formularios.FormNovaReservaDTO;
import com.ovg.transportes.dto.tela.formularios.NovaReservaRequestDTO;
import com.ovg.transportes.service.CidadeService;
import com.ovg.transportes.service.LocalAdministrativoService;
import com.ovg.transportes.service.SolicitacaoService;
import com.ovg.transportes.service.UsuarioAutenticadoProvider;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Modal "Criar Reservas" (Lista de Reservas e Calendario): monta o formulario
 * pronto pra exibir e cria a solicitacao, com as replicas em outros dias.
 */
@Service
public class NovaReservaService {

    private static final int MAX_DIAS_REPLICADOS = 5;

    private final SolicitacaoService solicitacaoService;
    private final LocalAdministrativoService localAdministrativoService;
    private final CidadeService cidadeService;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final ValidadorFormulario validador;

    public NovaReservaService(
        SolicitacaoService solicitacaoService,
        LocalAdministrativoService localAdministrativoService,
        CidadeService cidadeService,
        UsuarioAutenticadoProvider usuarioAutenticadoProvider,
        ValidadorFormulario validador
    ) {
        this.solicitacaoService = solicitacaoService;
        this.localAdministrativoService = localAdministrativoService;
        this.cidadeService = cidadeService;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
        this.validador = validador;
    }

    /**
     * acao: pra onde o form envia. filtros/urlFechar: pra voltar pra mesma tela.
     * digitado e erro: null na abertura normal; preenchidos quando o envio deu
     * erro e o modal precisa voltar aberto com o que a pessoa digitou.
     */
    public FormNovaReservaDTO formulario(String acao, String filtros, String urlFechar, NovaReservaRequestDTO digitado, String erro) {
        boolean aberto = digitado != null;
        NovaReservaRequestDTO form = aberto ? digitado : emBranco();
        List<LocalAdministrativoResponseDTO> locais = localAdministrativoService.listar();

        List<String> camposReplicar = new ArrayList<>();
        if (form.replicarDias() != null) {
            form.replicarDias().forEach(dia -> camposReplicar.add(dia == null ? "" : dia.toString()));
        }
        while (camposReplicar.size() < MAX_DIAS_REPLICADOS) {
            camposReplicar.add("");
        }

        return new FormNovaReservaDTO(
            acao,
            filtros,
            // o modal que volta aberto nao passa pelo JS do Bootstrap: as classes ja vao prontas
            aberto ? "modal fade show d-block" : "modal fade",
            aberto,
            erro,
            urlFechar,
            form.finalidade(),
            form.cidadeOrigem(),
            form.descricaoOrigem(),
            form.cidadeDestino(),
            form.descricaoDestino(),
            Formatos.campoDataHora(form.saida()),
            Formatos.campoDataHora(form.retorno()),
            camposReplicar.subList(0, MAX_DIAS_REPLICADOS),
            diasReplicados(form).isEmpty() ? "collapse cartao p-3 mt-2" : "collapse show cartao p-3 mt-2",
            form.qtdPassageiros(),
            form.telefone(),
            form.observacoes(),
            opcoesDeLocal(locais, form.localOrigemId(), form.cidadeOrigem()),
            opcoesDeLocal(locais, form.localDestinoId(), form.cidadeDestino()),
            cidadeService.listarNomesComUf()
        );
    }

    /** Cria a reserva e as replicas em outros dias; devolve a mensagem pra tela. */
    public String criar(NovaReservaRequestDTO form) {
        SolicitacaoRequestDTO base = paraRequisicao(form);
        validador.validar(base);

        List<SolicitacaoRequestDTO> replicas = new ArrayList<>();
        for (LocalDate dia : diasReplicados(form)) {
            // mesmo horario de saida; o retorno anda o mesmo tanto de dias,
            // pra uma viagem que volta no dia seguinte continuar voltando no dia seguinte
            long deslocamento = ChronoUnit.DAYS.between(base.dataHoraDesejada().toLocalDate(), dia);
            SolicitacaoRequestDTO replica = new SolicitacaoRequestDTO(
                base.localOrigemId(), base.cidadeOrigemId(), base.descricaoOrigem(),
                base.localDestinoId(), base.cidadeDestinoId(), base.descricaoDestino(),
                base.dataHoraDesejada().plusDays(deslocamento),
                base.dataHoraRetornoDesejada().plusDays(deslocamento),
                base.qtdPassageiros(), base.telefoneContato(), base.finalidade(), base.observacoes()
            );
            validador.validar(replica);
            replicas.add(replica);
        }

        // so grava depois de validar tudo, pra nao criar metade das replicas
        solicitacaoService.criar(base);
        replicas.forEach(solicitacaoService::criar);

        return replicas.isEmpty()
            ? "Solicitacao enviada com sucesso!"
            : "Solicitacao enviada e replicada para mais %d dia(s)!".formatted(replicas.size());
    }

    // formulario em branco: 1 passageiro e o telefone de quem esta logado
    private NovaReservaRequestDTO emBranco() {
        String telefone = usuarioAutenticadoProvider.obterUsuarioLogado().getTelefone();
        return new NovaReservaRequestDTO(null, null, null, null, null, null, null, null, null, List.of(), 1, telefone, null);
    }

    // unidades da OVG + "Outro local" no fim; marca o que ja estava escolhido
    // (na primeira abertura nada vem marcado e o navegador fica na primeira unidade)
    private static List<Opcao> opcoesDeLocal(List<LocalAdministrativoResponseDTO> locais, Long localEscolhido, String cidadeDigitada) {
        List<Opcao> opcoes = new ArrayList<>();
        locais.forEach(local -> opcoes.add(new Opcao(String.valueOf(local.id()), local.nome(), local.id().equals(localEscolhido))));
        opcoes.add(new Opcao("", "Outro local (informe a cidade)", localEscolhido == null && cidadeDigitada != null));
        return opcoes;
    }

    // unidade escolhida -> ignora cidade/descricao; "outro local" -> cidade (da lista) + descricao
    private SolicitacaoRequestDTO paraRequisicao(NovaReservaRequestDTO form) {
        Long cidadeOrigemId = form.localOrigemId() == null ? solicitacaoService.buscarIdCidadePorNomeComUf(form.cidadeOrigem()) : null;
        Long cidadeDestinoId = form.localDestinoId() == null ? solicitacaoService.buscarIdCidadePorNomeComUf(form.cidadeDestino()) : null;
        return new SolicitacaoRequestDTO(
            form.localOrigemId(), cidadeOrigemId, form.localOrigemId() == null ? vazioComoNulo(form.descricaoOrigem()) : null,
            form.localDestinoId(), cidadeDestinoId, form.localDestinoId() == null ? vazioComoNulo(form.descricaoDestino()) : null,
            form.saida(), form.retorno(), form.qtdPassageiros(), form.telefone(), form.finalidade(), vazioComoNulo(form.observacoes())
        );
    }

    // dias extras preenchidos, sem repetir o dia principal nem passar de 5
    private static List<LocalDate> diasReplicados(NovaReservaRequestDTO form) {
        if (form.replicarDias() == null) {
            return List.of();
        }
        LocalDate diaPrincipal = form.saida() == null ? null : form.saida().toLocalDate();
        return form.replicarDias().stream()
            .filter(Objects::nonNull)
            .filter(dia -> !dia.equals(diaPrincipal))
            .distinct()
            .limit(MAX_DIAS_REPLICADOS)
            .toList();
    }

    private static String vazioComoNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
