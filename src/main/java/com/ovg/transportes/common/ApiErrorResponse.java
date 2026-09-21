package com.ovg.transportes.common;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato padrao de erro devolvido por qualquer endpoint da API.
 */
public record ApiErrorResponse(
    LocalDateTime timestamp,
    int status,
    String mensagem,
    List<String> detalhes
) {

    public static ApiErrorResponse de(int status, String mensagem, List<String> detalhes) {
        return new ApiErrorResponse(LocalDateTime.now(), status, mensagem, detalhes);
    }
}
