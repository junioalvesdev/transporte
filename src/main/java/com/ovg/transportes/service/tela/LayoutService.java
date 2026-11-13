package com.ovg.transportes.service.tela;

import com.ovg.transportes.dto.comum.PaginaDTO;
import com.ovg.transportes.dto.tela.layout.ItemMenuDTO;
import com.ovg.transportes.dto.tela.layout.ItemPaginacaoDTO;
import com.ovg.transportes.dto.tela.layout.MenuDTO;
import com.ovg.transportes.dto.tela.layout.PaginacaoDTO;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Partes comuns a todas as paginas: o menu do topo (conforme o perfil de quem
 * esta logado) e a paginacao das listas.
 */
@Service
public class LayoutService {

    private record Secao(String texto, String url) {
    }

    // todo usuario logado ve estas
    private static final List<Secao> SECOES_TODOS = List.of(
        new Secao("Calendario", "/calendario"),
        new Secao("Minhas solicitacoes", "/minhas-solicitacoes")
    );

    // so quem tem perfil TRANSPORTE (as paginas tambem recusam os outros)
    private static final List<Secao> SECOES_TRANSPORTE = List.of(
        new Secao("Lista de Reservas", "/reservas"),
        new Secao("Solicitacoes pendentes", "/solicitacoes-pendentes"),
        new Secao("Veiculos", "/veiculos"),
        new Secao("Motoristas", "/motoristas"),
        new Secao("Relatorios", "/relatorios")
    );

    public MenuDTO montarMenu(Authentication autenticacao, String caminhoAtual) {
        boolean ehTransporte = ehTransporte(autenticacao);
        List<Secao> secoes = new ArrayList<>(SECOES_TODOS);
        if (ehTransporte) {
            secoes.addAll(SECOES_TRANSPORTE);
        }
        List<ItemMenuDTO> itens = secoes.stream()
            .map(secao -> new ItemMenuDTO(
                secao.texto(),
                secao.url(),
                caminhoAtual.equals(secao.url()) ? "dropdown-item active" : "dropdown-item"))
            .toList();
        return new MenuDTO(ehTransporte ? "Departamento de Transportes" : "Menu", itens);
    }

    public static boolean ehTransporte(Authentication autenticacao) {
        return autenticacao != null && autenticacao.getAuthorities().stream()
            .anyMatch(autoridade -> "ROLE_TRANSPORTE".equals(autoridade.getAuthority()));
    }

    /**
     * Botoes "Anterior 1 2 3 Proxima" pra uma lista paginada.
     * urlBase: endereco da pagina sem o numero, ex. "/veiculos?pagina=".
     */
    public static PaginacaoDTO paginacao(PaginaDTO<?> pagina, String urlBase) {
        if (pagina.totalPaginas() <= 1) {
            return new PaginacaoDTO(List.of(), "");
        }
        int atual = pagina.paginaAtual();
        int ultima = pagina.totalPaginas() - 1;
        List<ItemPaginacaoDTO> itens = new ArrayList<>();
        itens.add(botao("« Anterior", urlBase + (atual - 1), false, atual == 0));
        // no maximo 5 numeros em volta da pagina atual
        for (int numero = Math.max(0, atual - 2); numero <= Math.min(ultima, atual + 2); numero++) {
            itens.add(botao(String.valueOf(numero + 1), urlBase + numero, numero == atual, false));
        }
        itens.add(botao("Proxima »", urlBase + (atual + 1), false, atual == ultima));
        String legenda = "Pagina %d de %d · %d no total".formatted(atual + 1, pagina.totalPaginas(), pagina.totalElementos());
        return new PaginacaoDTO(itens, legenda);
    }

    private static ItemPaginacaoDTO botao(String texto, String url, boolean ativo, boolean desabilitado) {
        String classe = "btn btn-sm " + (ativo ? "btn-ovg-principal" : "btn-ovg-secundario") + (desabilitado ? " disabled" : "");
        return new ItemPaginacaoDTO(texto, url, classe);
    }
}
