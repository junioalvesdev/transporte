package com.ovg.transportes.controller;

import com.ovg.transportes.common.NegocioException;
import com.ovg.transportes.common.RecursoNaoEncontradoException;
import com.ovg.transportes.dto.veiculo.VeiculoRequestDTO;
import com.ovg.transportes.service.tela.VeiculosService;

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

/** Endpoints da pagina "Veiculos" (templates/veiculos/lista.html). */
@Controller
@RequestMapping("/veiculos")
@PreAuthorize("hasRole('TRANSPORTE')")
public class VeiculosController {

    private final VeiculosService veiculosService;

    public VeiculosController(VeiculosService veiculosService) {
        this.veiculosService = veiculosService;
    }

    // GET /veiculos?pagina=0
    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", veiculosService.montarLista(pagina));
        return "veiculos/lista";
    }

    @PostMapping
    public String cadastrar(@ModelAttribute VeiculoRequestDTO veiculo, RedirectAttributes redirect) {
        try {
            veiculosService.cadastrar(veiculo);
            redirect.addFlashAttribute("mensagem", "Veiculo cadastrado.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel cadastrar o veiculo: " + erro.getMessage());
        }
        return "redirect:/veiculos";
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable Long id,
        @ModelAttribute VeiculoRequestDTO veiculo,
        @RequestParam(defaultValue = "0") int pagina,
        RedirectAttributes redirect
    ) {
        try {
            veiculosService.atualizar(id, veiculo);
            redirect.addFlashAttribute("mensagem", "Veiculo atualizado.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel atualizar o veiculo: " + erro.getMessage());
        }
        return "redirect:/veiculos?pagina=" + pagina;
    }

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, @RequestParam(defaultValue = "0") int pagina, RedirectAttributes redirect) {
        try {
            veiculosService.inativar(id);
            redirect.addFlashAttribute("mensagem", "Veiculo inativado.");
        } catch (NegocioException | RecursoNaoEncontradoException erro) {
            redirect.addFlashAttribute("erro", "Nao foi possivel inativar o veiculo: " + erro.getMessage());
        }
        return "redirect:/veiculos?pagina=" + pagina;
    }
}
