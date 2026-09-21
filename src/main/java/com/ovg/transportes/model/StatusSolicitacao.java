package com.ovg.transportes.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum StatusSolicitacao {
    PENDENTE,
    APROVADA,
    REPROVADA,
    CANCELADA,
    ATENDIDA;

    private static final Map<StatusSolicitacao, Set<StatusSolicitacao>> TRANSICOES_VALIDAS = new EnumMap<>(StatusSolicitacao.class);

    static {
        TRANSICOES_VALIDAS.put(PENDENTE, Set.of(APROVADA, REPROVADA, CANCELADA));
        TRANSICOES_VALIDAS.put(APROVADA, Set.of(CANCELADA, ATENDIDA));
        TRANSICOES_VALIDAS.put(REPROVADA, Set.of());
        TRANSICOES_VALIDAS.put(CANCELADA, Set.of());
        TRANSICOES_VALIDAS.put(ATENDIDA, Set.of());
    }

    public boolean podeTransicionarPara(StatusSolicitacao novoStatus) {
        return TRANSICOES_VALIDAS.get(this).contains(novoStatus);
    }
}
