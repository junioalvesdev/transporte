package com.ovg.transportes.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum StatusViagem {
    PLANEJADA,
    ABERTA_PARA_APROVEITAMENTO,
    LOTADA,
    EM_ANDAMENTO,
    CONCLUIDA,
    CANCELADA;

    private static final Map<StatusViagem, Set<StatusViagem>> TRANSICOES_VALIDAS = new EnumMap<>(StatusViagem.class);

    static {
        TRANSICOES_VALIDAS.put(PLANEJADA, Set.of(ABERTA_PARA_APROVEITAMENTO, CANCELADA));
        TRANSICOES_VALIDAS.put(ABERTA_PARA_APROVEITAMENTO, Set.of(LOTADA, EM_ANDAMENTO, CANCELADA));
        TRANSICOES_VALIDAS.put(LOTADA, Set.of(ABERTA_PARA_APROVEITAMENTO, EM_ANDAMENTO));
        TRANSICOES_VALIDAS.put(EM_ANDAMENTO, Set.of(CONCLUIDA));
        TRANSICOES_VALIDAS.put(CONCLUIDA, Set.of());
        TRANSICOES_VALIDAS.put(CANCELADA, Set.of());
    }

    /**
     * Tabela de transicoes permitidas em vez de "if"s espalhados pelo service —
     * corrige o legado, onde `status` era varchar livre e qualquer valor podia
     * ser gravado em qualquer momento.
     */
    public boolean podeTransicionarPara(StatusViagem novoStatus) {
        return TRANSICOES_VALIDAS.get(this).contains(novoStatus);
    }
}
