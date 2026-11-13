package com.ovg.transportes.common.tela;

/**
 * Uma opcao de &lt;select&gt; ou checkbox pronta pra tela: o valor enviado no
 * formulario, o texto mostrado e se ja vem marcada. No HTML:
 * th:value="${o.valor}" th:text="${o.texto}" th:selected="${o.selecionado}".
 */
public record Opcao(String valor, String texto, boolean selecionado) {

    public static Opcao de(Object valor, String texto) {
        return new Opcao(String.valueOf(valor), texto, false);
    }
}
