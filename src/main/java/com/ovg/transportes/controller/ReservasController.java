package com.ovg.transportes.controller;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.tela.formularios.AprovarReservaRequestDTO;
import com.ovg.transportes.dto.tela.formularios.NovaReservaRequestDTO;
import com.ovg.transportes.dto.tela.reservas.FiltrosReservasDTO;
import com.ovg.transportes.service.tela.AprovacaoService;
import com.ovg.transportes.service.tela.NovaReservaService;
import com.ovg.transportes.service.tela.ReservasService;

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
 * Endpoints da pagina "Lista de Reservas" (templates/reservas/lista.html).
 * So recebe a requisicao, chama os services e escolhe a tela ou redireciona.
 * Acoes seguem Post/Redirect/Get: gravam e voltam pra lista com um aviso.
 * (A ordem de trafego e gerada pelo OrdensTrafegoController.)
 */
@Controller
@RequestMapping("/reservas")
@PreAuthorize("hasRole('TRANSPORTE')")
public class ReservasController {

    private final ReservasService reservasService;
    private final AprovacaoService aprovacaoService;
    private final NovaReservaService novaReservaService;

    public ReservasController(ReservasService reservasService, AprovacaoService aprovacaoService, NovaReservaService novaReservaService) {
        this.reservasService = reservasService;
        this.aprovacaoService = aprovacaoService;
        this.novaReservaService = novaReservaService;
    }

    // GET /reservas?status=...&de=...&ate=...
    @GetMapping
    public String listar(@ModelAttribute FiltrosReservasDTO filtros, Model model) {
        model.addAttribute("pagina", reservasService.montarLista(filtros, null, null));
        return "reservas/lista";
    }

    @PostMapping("/{id}/aprovar")
    public String aprovar(
        @PathVariable Long id,
        @ModelAttribute AprovarReservaRequestDTO requisicao,
        @RequestParam(defaultValue = "") String filtros,
        RedirectAttributes redirect
    ) {
        try {
            redirect.addFlashAttribute("mensagem", aprovacaoService.aprovar(id, requisicao));
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel aprovar a reserva #%d: %s".formatted(id, erro.getMessage()));
        }
        return redirecionarParaLista(filtros);
    }

    @PostMapping("/{id}/reprovar")
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
            redirect.addFlashAttribute("erro", "Nao foi possivel reprovar a reserva #%d: %s".formatted(id, erro.getMessage()));
        }
        return redirecionarParaLista(filtros);
    }

    @PostMapping("/nova")
    public String criar(
        @ModelAttribute NovaReservaRequestDTO form,
        @RequestParam(defaultValue = "") String filtros,
        Model model,
        RedirectAttributes redirect
    ) {
        try {
            redirect.addFlashAttribute("mensagem", novaReservaService.criar(form));
            return redirecionarParaLista(filtros);
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            // mostra a pagina de novo (sem redirect), com o modal aberto e o que
            // ja foi digitado, pra pessoa so corrigir o erro
            model.addAttribute("pagina", reservasService.montarLista(reservasService.filtrosDaQuery(filtros), form, erro.getMessage()));
            return "reservas/lista";
        }
    }

    // "filtros" vem de um campo escondido do proprio form — so aceita o formato
    // de query string que a tela gera, pra nao virar redirecionamento arbitrario
    private static String redirecionarParaLista(String filtros) {
        String query = filtros.matches("[A-Za-z0-9=&_%.\\-]*") ? filtros : "";
        return "redirect:/reservas" + (query.isEmpty() ? "" : "?" + query);
    }
}
