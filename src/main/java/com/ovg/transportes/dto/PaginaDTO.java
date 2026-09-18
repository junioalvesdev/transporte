package com.ovg.transportes.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope de paginacao generico para qualquer listagem da API — evita
 * devolver Page<Entity> do Spring Data direto (vazaria a entidade JPA) e
 * padroniza o formato que o frontend consome em todas as telas com lista.
 */
public record PaginaDTO<T>(
    List<T> conteudo,
    int paginaAtual,
    int totalPaginas,
    long totalElementos
) {

    public static <E, T> PaginaDTO<T> de(Page<E> pagina, Function<E, T> mapeador) {
        return new PaginaDTO<>(
            pagina.getContent().stream().map(mapeador).toList(),
            pagina.getNumber(),
            pagina.getTotalPages(),
            pagina.getTotalElements()
        );
    }
}
