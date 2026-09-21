/**
 * Modal "Minhas solicitacoes", acessivel pelo menu de qualquer pagina
 * (calendario.html#minhas). Carrega os dados so na primeira vez que o modal e
 * aberto (nao no carregamento da pagina), e tambem quando uma solicitacao
 * nova e criada ou uma vaga e solicitada em outro modal.
 */
const classePorStatusSolicitacao = {
    PENDENTE: "status-badge-pendente",
    APROVADA: "status-badge-aprovada",
    ATENDIDA: "status-badge-aprovada",
    REPROVADA: "status-badge-reprovada",
    CANCELADA: "status-badge-cancelada",
};

let minhasSolicitacoesCarregadas = false;

document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("modal-minhas-solicitacoes");
    if (!modal) {
        return;
    }
    modal.addEventListener("shown.bs.modal", () => carregarMinhasSolicitacoes());

    // vindo do menu com "calendario.html#minhas", abre o modal direto
    const abrirSeHashMinhas = () => {
        if (window.location.hash === "#minhas") {
            bootstrap.Modal.getOrCreateInstance(modal).show();
        }
    };
    abrirSeHashMinhas();
    window.addEventListener("hashchange", abrirSeHashMinhas);
});

async function carregarMinhasSolicitacoes(pagina = 0) {
    minhasSolicitacoesCarregadas = true;
    const lista = document.getElementById("lista-minhas-solicitacoes");
    try {
        const resultado = await Api.get(`/api/solicitacoes/minhas?pagina=${pagina}&tamanho=10`);
        lista.innerHTML = "";
        resultado.conteudo.map(criarCartaoSolicitacao).forEach((cartao) => lista.appendChild(cartao));
        if (resultado.conteudo.length === 0) {
            lista.innerHTML = '<p class="text-muted">Voce ainda nao fez nenhuma solicitacao.</p>';
        }
        Ui.montarPaginacao(document.getElementById("paginacao-minhas-solicitacoes"), resultado, carregarMinhasSolicitacoes);
    } catch (erro) {
        lista.innerHTML = `<p class="text-danger">Erro ao carregar suas solicitacoes: ${erro.message}</p>`;
    }
}

function criarCartaoSolicitacao(solicitacao) {
    const dataHora = new Date(solicitacao.dataHoraDesejada);
    const statusCancelavel = solicitacao.status === "PENDENTE" || solicitacao.status === "APROVADA";
    const prazoVencido = dataHora.getTime() <= Date.now();
    const podeCancel = statusCancelavel && !prazoVencido;

    let acaoHtml = "";
    if (podeCancel) {
        acaoHtml = `<div><button class="btn btn-ovg-secundario btn-sm" data-id="${solicitacao.id}">Cancelar</button></div>`;
    } else if (statusCancelavel && prazoVencido) {
        // mesma regra do backend (Solicitacao.cancelar): depois do horario
        // desejado, a solicitacao nao pode mais ser cancelada — aqui so
        // deixamos isso visivel ANTES do clique, em vez do usuario
        // descobrir com um erro depois de confirmar.
        acaoHtml = `<div class="text-muted small">Prazo para cancelamento encerrado</div>`;
    }

    const cartao = document.createElement("div");
    cartao.className = "cartao cartao-viagem d-flex justify-content-between align-items-center flex-wrap gap-3";
    cartao.innerHTML = `
        <div>
            <div class="rota-titulo">${solicitacao.origemExibicao} &rarr; ${solicitacao.destinoExibicao}</div>
            <div class="text-muted small">${dataHora.toLocaleString("pt-BR")} &middot; ${solicitacao.qtdPassageiros} passageiro(s)</div>
            ${solicitacao.motivoRecusa ? `<div class="text-danger small mt-1">Motivo da recusa: ${solicitacao.motivoRecusa}</div>` : ""}
        </div>
        <div class="text-end">
            <span class="badge ${classePorStatusSolicitacao[solicitacao.status] || "bg-secondary"} mb-2 d-inline-block">${solicitacao.status}</span>
            ${acaoHtml}
        </div>
    `;

    const botaoCancelar = cartao.querySelector("button");
    if (botaoCancelar) {
        botaoCancelar.addEventListener("click", () => cancelarSolicitacao(solicitacao.id));
    }
    return cartao;
}

async function cancelarSolicitacao(id) {
    const confirmado = await Ui.confirmarAcao("Cancelar esta solicitacao?", "Cancelar solicitacao");
    if (!confirmado) {
        return;
    }
    try {
        await Api.patch(`/api/solicitacoes/${id}/cancelar`);
        Ui.mostrarToast("Solicitacao cancelada.");
        await carregarMinhasSolicitacoes();
    } catch (erro) {
        Ui.mostrarToast(erro.message, "erro");
    }
}
