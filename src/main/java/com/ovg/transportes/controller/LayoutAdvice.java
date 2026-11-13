package com.ovg.transportes.controller;

import com.ovg.transportes.dto.tela.layout.MenuDTO;
import com.ovg.transportes.service.tela.LayoutService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Coloca o menu do topo (${menu}) em todas as paginas, sem cada controller
 * precisar fazer isso. As chamadas da API JSON (/api/...) ficam de fora.
 */
@ControllerAdvice(basePackageClasses = LayoutAdvice.class)
public class LayoutAdvice {

    private final LayoutService layoutService;

    public LayoutAdvice(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @ModelAttribute("menu")
    public MenuDTO menu(Authentication autenticacao, HttpServletRequest request) {
        if (autenticacao == null || request.getRequestURI().startsWith("/api/")) {
            return null;
        }
        return layoutService.montarMenu(autenticacao, request.getRequestURI());
    }
}
