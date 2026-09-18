/**
 * Tela "Central de Transportes": lista viagens abertas num dia e permite
 * solicitar vaga (aproveitamento) numa delas — sem precisar informar
 * origem/destino de antemao, cada card ja mostra a rota da viagem.
 */
document.addEventListener("DOMContentLoaded", () => {
    const formulario = document.getElementById("form-busca-viagem");
    const listaResultados = document.getElementById("lista-resultados");
    const mensagemVazio = document.getElementById("mensagem-sem-resultados");

    // impede escolher uma data que ja passou (o backend so aceita hoje em diante).
    // Usa o horario LOCAL do navegador, nao UTC — toISOString() pode cair no
    // dia seguinte perto da meia-noite e bloquear o dia de hoje por engano.
    const hoje = new Date();
    const hojeFormatado = `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, "0")}-${String(hoje.getDate()).padStart(2, "0")}`;
    document.getElementById("data-busca").min = hojeFormatado;
    document.getElementById("data-busca").value = hojeFormatado;

    formulario.addEventListener("submit", async (evento) => {
        evento.preventDefault();
        await buscarViagens();
    });

    // busca ja de cara com a data de hoje, sem esperar o usuario clicar em "Buscar"
    buscarViagens();

    async function buscarViagens() {
        const parametros = new URLSearchParams({
            data: document.getElementById("data-busca").value,
            qtdPassageiros: document.getElementById("qtd-passageiros").value,
        });

        try {
            const viagens = await Api.get(`/api/viagens/disponiveis?${parametros.toString()}`);
            renderizarResultados(viagens);
        } catch (erro) {
            Ui.mostrarToast(erro.message, "erro");
        }
    }

    function renderizarResultados(viagens) {
        listaResultados.innerHTML = "";
        mensagemVazio.classList.toggle("d-none", viagens.length > 0);

        // sem "for": cada viagem vira um cartao, um de cada vez, na ordem que a API ja devolveu (por horario de saida)
        viagens.map(criarCartaoViagem).forEach((cartao) => listaResultados.appendChild(cartao));
    }

    function criarCartaoViagem(viagem) {
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

        cartao.querySelector("button").addEventListener("click", () => solicitarVaga(viagem));
        return cartao;
    }

    async function solicitarVaga(viagem) {
        const qtdPassageiros = document.getElementById("qtd-passageiros").value;

        try {
            await Api.post(`/api/viagens/${viagem.viagemId}/participantes`, {
                qtdPassageiros: Number(qtdPassageiros),
                cidadeEmbarqueId: viagem.cidadeOrigemId,
                cidadeDesembarqueId: viagem.cidadeDestinoId,
            });
            Ui.mostrarToast("Vaga solicitada com sucesso!");
            await buscarViagens();
        } catch (erro) {
            Ui.mostrarToast(erro.message, "erro");
        }
    }
});
