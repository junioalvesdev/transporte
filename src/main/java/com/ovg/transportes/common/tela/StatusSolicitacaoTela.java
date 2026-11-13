package com.ovg.transportes.common.tela;

import com.ovg.transportes.model.StatusSolicitacao;

import java.util.Map;

/**
 * Como o status de uma solicitacao aparece nas paginas: o texto e a cor do
 * selo, iguais aos da tela equivalente do sistema legado.
 */
public final class StatusSolicitacaoTela {

    private static final Map<StatusSolicitacao, String> ROTULO = Map.of(
        StatusSolicitacao.PENDENTE, "Aguardando",
        StatusSolicitacao.APROVADA, "Disponivel",
        StatusSolicitacao.ATENDIDA, "Atendida",
        StatusSolicitacao.REPROVADA, "Reprovada",
        StatusSolicitacao.CANCELADA, "Cancelada"
    );

    private static final Map<StatusSolicitacao, String> CLASSE = Map.of(
        StatusSolicitacao.PENDENTE, "status-badge-pendente",
        StatusSolicitacao.APROVADA, "status-badge-aprovada",
        StatusSolicitacao.ATENDIDA, "status-badge-aprovada",
        StatusSolicitacao.REPROVADA, "status-badge-reprovada",
        StatusSolicitacao.CANCELADA, "status-badge-cancelada"
    );

    private StatusSolicitacaoTela() {
    }

    // "Aguardando", "Disponivel"...
    public static String rotulo(StatusSolicitacao status) {
        return ROTULO.get(status);
    }

    // classes CSS completas do selo colorido
    public static String classe(StatusSolicitacao status) {
        return "badge status-badge " + CLASSE.get(status);
    }
}
