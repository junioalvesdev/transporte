package com.ovg.transportes.common.tela;

import com.ovg.transportes.model.StatusSolicitacao;
import com.ovg.transportes.model.StatusViagem;

import java.util.Map;

/**
 * Como os status aparecem no calendario: texto e cor das "pilulas" de viagem
 * e das solicitacoes que ainda nao viraram viagem (classes em calendario.css).
 */
public final class StatusViagemTela {

    private static final Map<StatusViagem, String> ROTULO = Map.of(
        StatusViagem.PLANEJADA, "Planejada",
        StatusViagem.ABERTA_PARA_APROVEITAMENTO, "Aberta p/ aproveitamento",
        StatusViagem.LOTADA, "Lotada",
        StatusViagem.EM_ANDAMENTO, "Em andamento",
        StatusViagem.CONCLUIDA, "Concluida",
        StatusViagem.CANCELADA, "Cancelada"
    );

    private static final Map<StatusViagem, String> COR = Map.of(
        StatusViagem.PLANEJADA, "cor-planejada",
        StatusViagem.ABERTA_PARA_APROVEITAMENTO, "cor-aberta",
        StatusViagem.LOTADA, "cor-lotada",
        StatusViagem.EM_ANDAMENTO, "cor-andamento",
        StatusViagem.CONCLUIDA, "cor-concluida",
        StatusViagem.CANCELADA, "cor-cancelada"
    );

    // solicitacoes sem viagem ainda (as aprovadas aparecem pela propria viagem)
    private static final Map<StatusSolicitacao, String> ROTULO_SOLICITACAO = Map.of(
        StatusSolicitacao.PENDENTE, "Pendente",
        StatusSolicitacao.REPROVADA, "Reprovada",
        StatusSolicitacao.CANCELADA, "Cancelada"
    );

    private static final Map<StatusSolicitacao, String> COR_SOLICITACAO = Map.of(
        StatusSolicitacao.PENDENTE, "cor-solicitacao-pendente",
        StatusSolicitacao.REPROVADA, "cor-solicitacao-reprovada",
        StatusSolicitacao.CANCELADA, "cor-solicitacao-cancelada"
    );

    private StatusViagemTela() {
    }

    public static String rotulo(StatusViagem status) {
        return ROTULO.get(status);
    }

    // so a cor ("cor-planejada"...), pra bolinha do filtro e pra pilula
    public static String cor(StatusViagem status) {
        return COR.getOrDefault(status, "cor-planejada");
    }

    public static String rotulo(StatusSolicitacao status) {
        return ROTULO_SOLICITACAO.getOrDefault(status, status.name());
    }

    public static String cor(StatusSolicitacao status) {
        return COR_SOLICITACAO.getOrDefault(status, "cor-solicitacao-pendente");
    }
}
