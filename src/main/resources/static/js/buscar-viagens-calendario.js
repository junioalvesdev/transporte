/**
 * Filtro "Buscar viagens" no cabecalho do calendario: mesma busca por viagens
 * abertas pra aproveitamento que antes vivia numa aba da tela inicial — agora
 * o resultado aparece num modal em vez de ocupar espaco fixo na pagina.
 */
let modalResultadosBusca = null;

document.addEventListener("DOMContentLoaded", () => {
    modalResultadosBusca = new bootstrap.Modal(document.getElementById("modal-resultados-busca"));

    // impede escolher uma data que ja passou (o backend so aceita hoje em diante)
    const hoje = new Date();
    const hojeFormatado = `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, "0")}-${String(hoje.getDate()).padStart(2, "0")}`;
    document.getElementById("data-busca-calendario").min = hojeFormatado;
    document.getElementById("data-busca-calendario").value = hojeFormatado;

    document.getElementById("form-busca-viagem-calendario").addEventListener("submit", async (evento) => {
        evento.preventDefault();
        await buscarViagensCalendario();
    });

    document.getElementById("botao-criar-solicitacao-sem-resultado").addEventListener("click", () => {
        modalResultadosBusca.hide();
        bootstrap.Modal.getOrCreateInstance(document.getElementById("modal-nova-solicitacao")).show();
    });
});

async function buscarViagensCalendario() {
    const parametros = new URLSearchParams({
        data: document.getElementById("data-busca-calendario").value,
        qtdPassageiros: document.getElementById("qtd-passageiros-calendario").value,
    });

    try {
        const viagens = await Api.get(`/api/viagens/disponiveis?${parametros.toString()}`);
        renderizarResultadosBusca(viagens);
        modalResultadosBusca.show();
    } catch (erro) {
        Ui.mostrarToast(erro.message, "erro");
    }
}

function renderizarResultadosBusca(viagens) {
    const lista = document.getElementById("lista-resultados-busca");
    const vazio = document.getElementById("mensagem-sem-resultados-busca");
    lista.innerHTML = "";
    vazio.classList.toggle("d-none", viagens.length > 0);
    viagens.map(criarCartaoViagemBusca).forEach((cartao) => lista.appendChild(cartao));
}

function criarCartaoViagemBusca(viagem) {
    const saida = new Date(viagem.dataHoraSaida);
    const chegada = viagem.dataHoraChegadaEstimada ? new Date(viagem.dataHoraChegadaEstimada) : null;
    const vagasClasse = viagem.vagasDisponiveis <= 3 ? "baixa" : "";

    const cartao = document.createElement("div");
    cartao.className = "cartao cartao-viagem d-flex justify-content-between align-items-center flex-wrap gap-3";
    cartao.innerHTML = `
        <div>
            <div class="rota-titulo">${viagem.origem.toUpperCase()} &rarr; ${viagem.destino.toUpperCase()}</div>
            <div class="text-muted small">${saida.toLocaleDateString("pt-BR")}</div>
            <div class="mt-1">
                Saida: <strong>${saida.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })}</strong>
                ${chegada ? ` &middot; Chegada estimada: <strong>${chegada.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })}</strong>` : ""}
            </div>
            <div class="mt-1 text-muted"><i class="bi ${iconeParaTipoVeiculo(viagem.tipoVeiculo)} me-1"></i>${viagem.tipoVeiculo}</div>
        </div>
        <div class="text-end">
            <span class="badge badge-vagas ${vagasClasse} mb-2 d-inline-block">${viagem.vagasDisponiveis} lugares disponiveis</span>
            <div>
                <button class="btn btn-ovg-principal btn-sm" data-viagem-id="${viagem.viagemId}">Solicitar vaga</button>
            </div>
        </div>
    `;

    cartao.querySelector("button").addEventListener("click", () => solicitarVagaBusca(viagem));
    return cartao;
}

async function solicitarVagaBusca(viagem) {
    const qtdPassageiros = document.getElementById("qtd-passageiros-calendario").value;

    try {
        await Api.post(`/api/viagens/${viagem.viagemId}/participantes`, {
            qtdPassageiros: Number(qtdPassageiros),
            cidadeEmbarqueId: viagem.cidadeOrigemId,
            cidadeDesembarqueId: viagem.cidadeDestinoId,
        });
        Ui.mostrarToast("Vaga solicitada com sucesso!");
        await buscarViagensCalendario();
        // se a lista de "Minhas solicitacoes" ja foi aberta antes, atualiza ela tambem
        if (typeof minhasSolicitacoesCarregadas !== "undefined" && minhasSolicitacoesCarregadas) {
            await carregarMinhasSolicitacoes();
        }
    } catch (erro) {
        Ui.mostrarToast(erro.message, "erro");
    }
}
