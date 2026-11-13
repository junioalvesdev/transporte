package com.ovg.transportes.common.tela;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Formatacoes usadas pelas telas renderizadas no servidor — tudo que o HTML
 * mostra ja sai daqui como texto pronto, em vez de ser formatado no template.
 */
public final class Formatos {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    // formato que o <input type="datetime-local"> entende
    private static final DateTimeFormatter CAMPO_DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static final String[] MESES = {
        "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    private Formatos() {
    }

    // "21/09/2026 18:29", ou "-" quando nao tem data
    public static String dataHora(LocalDateTime valor) {
        return valor == null ? "-" : valor.format(DATA_HORA);
    }

    // "21/09/2026"
    public static String data(LocalDate valor) {
        return valor == null ? "-" : valor.format(DATA);
    }

    // "18:29"
    public static String hora(LocalDateTime valor) {
        return valor == null ? "-" : valor.format(HORA);
    }

    // "2026-09-21T18:29" pro valor inicial de um campo datetime-local, ou vazio
    public static String campoDataHora(LocalDateTime valor) {
        return valor == null ? "" : valor.format(CAMPO_DATA_HORA);
    }

    // "Setembro 2026"
    public static String mesAno(YearMonth mes) {
        return "%s %d".formatted(MESES[mes.getMonthValue() - 1], mes.getYear());
    }

    // "(62) 99999-9999" (celular) ou "(62) 3333-3333" (fixo); qualquer outra
    // coisa (numero incompleto, ramal...) volta como veio, e vazio vira "-"
    public static String telefone(String valor) {
        if (valor == null || valor.isBlank()) {
            return "-";
        }
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() == 11) {
            return "(%s) %s-%s".formatted(digitos.substring(0, 2), digitos.substring(2, 7), digitos.substring(7));
        }
        if (digitos.length() == 10) {
            return "(%s) %s-%s".formatted(digitos.substring(0, 2), digitos.substring(2, 6), digitos.substring(6));
        }
        return valor;
    }

    // icone do Bootstrap Icons mais parecido com o tipo de veiculo (so visual)
    public static String iconeTipoVeiculo(String tipo) {
        String nome = tipo == null ? "" : tipo.toLowerCase();
        if (nome.contains("caminh")) return "bi bi-truck";
        if (nome.contains("moto")) return "bi bi-scooter";
        if (nome.contains("onibus") || nome.contains("bus")) return "bi bi-bus-front-fill";
        if (nome.contains("ambul") || nome.contains("van") || nome.contains("kombi") || nome.contains("furg")) return "bi bi-truck-front-fill";
        return "bi bi-car-front-fill";
    }

    // texto vazio vira "-" pra celula da tabela nao ficar em branco
    public static String textoOuTraco(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }
}
