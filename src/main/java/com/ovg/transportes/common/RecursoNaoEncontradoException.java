package com.ovg.transportes.common;

/**
 * Sinaliza que um recurso buscado por id nao existe. Sempre vira HTTP 404
 * no GlobalExceptionHandler.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
