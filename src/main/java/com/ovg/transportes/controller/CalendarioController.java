package com.ovg.transportes.controller;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.tela.calendario.EditarSolicitacaoRequestDTO;
import com.ovg.transportes.dto.tela.calendario.EditarViagemRequestDTO;
import com.ovg.transportes.dto.tela.calendario.FiltrosCalendarioDTO;
import com.ovg.transportes.dto.tela.formularios.AprovarReservaRequestDTO;
import com.ovg.transportes.dto.tela.formularios.NovaReservaRequestDTO;
import com.ovg.transportes.service.tela.AprovacaoService;
import com.ovg.transportes.service.tela.CalendarioService;
import com.ovg.transportes.service.tela.NovaReservaService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Endpoints da pagina "Calendario" (templates/calendario/calendario.html),
 * a pagina principal do sistema. So recebe a requisicao, chama os services e
 * escolhe a tela ou redireciona (Post/Redirect/Get, voltando pro calendario
 * exatamente como estava, pelo campo escondido "filtros").
 */
@Controller
@RequestMapping("/calendario")
public class CalendarioController {

    private final CalendarioService calendarioService;
    private final AprovacaoService aprovacaoService;
    private final NovaReservaService novaReservaService;

    public CalendarioController(CalendarioService calendarioService, AprovacaoService aprovacaoService, NovaReservaService novaReservaService) {
        this.calendarioService = calendarioService;
        this.aprovacaoService = aprovacaoService;
        this.novaReservaService = novaReservaService;
    }

    // GET /calendario?modo=mes&data=2026-09-22&status=...&busca=2026-09-25&passageiros=1
    @GetMapping
    public String mostrar(@ModelAttribute FiltrosCalendarioDTO filtros, Model model) {
        model.addAttribute("pagina", calendarioService.montar(filtros, null, null));
        return "calendario/calendario";
    }

    // ---------------------------------------------------------------- qualquer usuario

    @PostMapping("/nova")
    public String criarReserva(
        @ModelAttribute NovaReservaRequestDTO form,
        @RequestParam(defaultValue = "") String filtros,
        Model model,
        RedirectAttributes redirect
    ) {
        try {
            redirect.addFlashAttribute("mensagem", novaReservaService.criar(form));
            return voltar(filtros);
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            // mostra o calendario de novo (sem redirect), com o modal aberto e o
            // que ja foi digitado, pra pessoa so corrigir o erro
            model.addAttribute("pagina", calendarioService.montar(calendarioService.filtrosDaQuery(filtros), form, erro.getMessage()));
            return "calendario/calendario";
        }
    }

    // quem pediu corrige a propria solicitacao pendente
    @PostMapping("/solicitacoes/{id}")
    public String editarSolicitacao(
        @PathVariable Long id,
        @ModelAttribute EditarSolicitacaoRequestDTO form,
        @RequestParam(defaultValue = "") String filtros,
        RedirectAttributes redirect
    ) {
        try {
            calendarioService.editarSolicitacao(id, form);
            redirect.addFlashAttribute("mensagem", "Solicitacao atualizada.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel atualizar a solicitacao: " + erro.getMessage());
        }
        return voltar(filtros);
    }

    // cancelar a propria solicitacao, vaga de carona ou participacao numa viagem
    @PostMapping("/solicitacoes/{id}/cancelar")
    public String cancelarSolicitacao(@PathVariable Long id, @RequestParam(defaultValue = "") String filtros, RedirectAttributes redirect) {
        try {
            calendarioService.cancelarSolicitacao(id);
            redirect.addFlashAttribute("mensagem", "Cancelado.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel cancelar: " + erro.getMessage());
        }
        return voltar(filtros);
    }

    // busca de carona: pega vaga numa viagem aberta pra aproveitamento
    @PostMapping("/viagens/{id}/vaga")
    public String solicitarVaga(
        @PathVariable Long id,
        @RequestParam(required = false) Integer passageiros,
        @RequestParam(required = false) Long cidadeEmbarqueId,
        @RequestParam(required = false) Long cidadeDesembarqueId,
        @RequestParam(defaultValue = "") String filtros,
        RedirectAttributes redirect
    ) {
        try {
            calendarioService.solicitarVaga(id, passageiros, cidadeEmbarqueId, cidadeDesembarqueId);
            redirect.addFlashAttribute("mensagem", "Vaga solicitada com sucesso!");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel solicitar a vaga: " + erro.getMessage());
        }
        return voltar(filtros);
    }

    // ---------------------------------------------------------------- so TRANSPORTE

    @PostMapping("/viagens/{id}")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public String editarViagem(
        @PathVariable Long id,
        @ModelAttribute EditarViagemRequestDTO form,
        @RequestParam(defaultValue = "") String filtros,
        RedirectAttributes redirect
    ) {
        try {
            calendarioService.editarViagem(id, form);
            redirect.addFlashAttribute("mensagem", "Viagem atualizada.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel atualizar a viagem: " + erro.getMessage());
        }
        return voltar(filtros);
    }

    @PostMapping("/solicitacoes/{id}/aprovar")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public String aprovar(
        @PathVariable Long id,
        @ModelAttribute AprovarReservaRequestDTO requisicao,
        @RequestParam(defaultValue = "") String filtros,
        RedirectAttributes redirect
    ) {
        try {
            redirect.addFlashAttribute("mensagem", aprovacaoService.aprovar(id, requisicao));
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel aprovar a solicitacao: " + erro.getMessage());
        }
        return voltar(filtros);
    }

    @PostMapping("/solicitacoes/{id}/reprovar")
    @PreAuthorize("hasRole('TRANSPORTE')")
    public String reprovar(
        @PathVariable Long id,
        @RequestParam(defaultValue = "") String motivoRecusa,
        @RequestParam(defaultValue = "") String filtros,
        RedirectAttributes redirect
    ) {
        try {
            aprovacaoService.reprovar(id, motivoRecusa);
            redirect.addFlashAttribute("mensagem", "Solicitacao reprovada.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel reprovar a solicitacao: " + erro.getMessage());
        }
        return voltar(filtros);
    }

    // "filtros" vem de um campo escondido do proprio form — so aceita o formato
    // de query string que a pagina gera, pra nao virar redirecionamento arbitrario
    private static String voltar(String filtros) {
        String query = filtros.matches("[A-Za-z0-9=&_%.\\-]*") ? filtros : "";
        return "redirect:/calendario" + (query.isEmpty() ? "" : "?" + query);
    }
}
