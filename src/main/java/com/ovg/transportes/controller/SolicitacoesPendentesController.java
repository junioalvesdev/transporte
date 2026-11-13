package com.ovg.transportes.controller;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.tela.formularios.AprovarReservaRequestDTO;
import com.ovg.transportes.service.tela.AprovacaoService;
import com.ovg.transportes.service.tela.SolicitacoesPendentesService;

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

/** Endpoints da pagina "Solicitacoes pendentes" (templates/pendentes/lista.html). */
@Controller
@RequestMapping("/solicitacoes-pendentes")
@PreAuthorize("hasRole('TRANSPORTE')")
public class SolicitacoesPendentesController {

    private final SolicitacoesPendentesService pendentesService;
    private final AprovacaoService aprovacaoService;

    public SolicitacoesPendentesController(SolicitacoesPendentesService pendentesService, AprovacaoService aprovacaoService) {
        this.pendentesService = pendentesService;
        this.aprovacaoService = aprovacaoService;
    }

    // GET /solicitacoes-pendentes?pagina=0
    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", pendentesService.montarLista(pagina));
        return "pendentes/lista";
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
            redirect.addFlashAttribute("erro", "Nao foi possivel aprovar a solicitacao #%d: %s".formatted(id, erro.getMessage()));
        }
        return voltar(filtros);
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
            redirect.addFlashAttribute("erro", "Nao foi possivel reprovar a solicitacao #%d: %s".formatted(id, erro.getMessage()));
        }
        return voltar(filtros);
    }

    // "filtros" e o "pagina=N" que a propria tela envia — so aceita esse formato
    private static String voltar(String filtros) {
        return "redirect:/solicitacoes-pendentes" + (filtros.matches("pagina=\\d+") ? "?" + filtros : "");
    }
}
