package com.ovg.transportes.common;

/**
 * Sinaliza violacao de uma regra de negocio (ex.: transicao de status invalida,
 * viagem sem vaga suficiente). Sempre vira HTTP 422 no GlobalExceptionHandler.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
