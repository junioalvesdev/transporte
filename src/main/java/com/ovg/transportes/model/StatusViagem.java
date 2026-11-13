package com.ovg.transportes.model;

public enum StatusViagem {
    PLANEJADA,
    ABERTA_PARA_APROVEITAMENTO,
    LOTADA,
    EM_ANDAMENTO,
    CONCLUIDA,
    CANCELADA;

    /**
     * Qualquer status pode ir para qualquer outro — o Departamento de
     * Transportes edita o status manualmente pelo calendario/reservas, e uma
     * pessoa pode selecionar o status errado por engano e precisar voltar
     * atras (ex.: marcou EM_ANDAMENTO sem querer, precisa voltar pra
     * ABERTA_PARA_APROVEITAMENTO). Sem trava nenhuma aqui, de proposito.
     */
    public boolean podeTransicionarPara(StatusViagem novoStatus) {
        return true;
    }
}
