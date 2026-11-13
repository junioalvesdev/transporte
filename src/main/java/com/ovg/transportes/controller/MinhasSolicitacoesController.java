package com.ovg.transportes.controller;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.service.tela.MinhasSolicitacoesService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Endpoints da pagina "Minhas solicitacoes" (templates/minhas/lista.html) — qualquer usuario logado. */
@Controller
@RequestMapping("/minhas-solicitacoes")
public class MinhasSolicitacoesController {

    private final MinhasSolicitacoesService minhasService;

    public MinhasSolicitacoesController(MinhasSolicitacoesService minhasService) {
        this.minhasService = minhasService;
    }

    // GET /minhas-solicitacoes?pagina=0
    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", minhasService.montarLista(pagina));
        return "minhas/lista";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @RequestParam(defaultValue = "0") int pagina, RedirectAttributes redirect) {
        try {
            minhasService.cancelar(id);
            redirect.addFlashAttribute("mensagem", "Solicitacao cancelada.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel cancelar: " + erro.getMessage());
        }
        return "redirect:/minhas-solicitacoes?pagina=" + pagina;
    }
}
