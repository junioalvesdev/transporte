package com.ovg.transportes.controller;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.motorista.MotoristaRequestDTO;
import com.ovg.transportes.service.tela.MotoristasService;

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

/** Endpoints da pagina "Motoristas" (templates/motoristas/lista.html). */
@Controller
@RequestMapping("/motoristas")
@PreAuthorize("hasRole('TRANSPORTE')")
public class MotoristasController {

    private final MotoristasService motoristasService;

    public MotoristasController(MotoristasService motoristasService) {
        this.motoristasService = motoristasService;
    }

    // GET /motoristas?pagina=0
    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", motoristasService.montarLista(pagina));
        return "motoristas/lista";
    }

    @PostMapping
    public String cadastrar(@ModelAttribute MotoristaRequestDTO motorista, RedirectAttributes redirect) {
        try {
            motoristasService.cadastrar(motorista);
            redirect.addFlashAttribute("mensagem", "Motorista cadastrado.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel cadastrar o motorista: " + erro.getMessage());
        }
        return "redirect:/motoristas";
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable Long id,
        @ModelAttribute MotoristaRequestDTO motorista,
        @RequestParam(defaultValue = "0") int pagina,
        RedirectAttributes redirect
    ) {
        try {
            motoristasService.atualizar(id, motorista);
            redirect.addFlashAttribute("mensagem", "Motorista atualizado.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel atualizar o motorista: " + erro.getMessage());
        }
        return "redirect:/motoristas?pagina=" + pagina;
    }

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, @RequestParam(defaultValue = "0") int pagina, RedirectAttributes redirect) {
        try {
            motoristasService.inativar(id);
            redirect.addFlashAttribute("mensagem", "Motorista inativado.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel inativar o motorista: " + erro.getMessage());
        }
        return "redirect:/motoristas?pagina=" + pagina;
    }
}
