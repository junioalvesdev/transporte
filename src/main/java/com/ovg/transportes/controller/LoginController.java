package com.ovg.transportes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Tela de login (templates/login.html). So mostra o formulario: quem recebe o
 * POST /login e autentica e o Spring Security (ver SecurityConfig.formLogin).
 */
@Controller
public class LoginController {

    // GET /login, ou /login?erro depois de usuario/senha errados
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String erro, Model model) {
        model.addAttribute("erroLogin", erro != null ? "Usuario ou senha invalidos." : null);
        return "login";
    }

    // "/" leva pra pagina principal (o calendario)
    @GetMapping("/")
    public String inicio() {
        return "redirect:/calendario";
    }
}
