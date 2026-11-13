package com.ovg.transportes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // as paginas deixaram de ser HTML estaticos (agora sao renderizadas no
    // servidor, sem .html no endereco) — favoritos e links antigos continuam funcionando
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/index.html", "/calendario");
        registry.addRedirectViewController("/login.html", "/login");
        registry.addRedirectViewController("/calendario.html", "/calendario");
        registry.addRedirectViewController("/reservas.html", "/reservas");
        registry.addRedirectViewController("/solicitacoes-pendentes.html", "/solicitacoes-pendentes");
        registry.addRedirectViewController("/veiculos.html", "/veiculos");
        registry.addRedirectViewController("/motoristas.html", "/motoristas");
        registry.addRedirectViewController("/relatorios.html", "/relatorios");
    }
}
