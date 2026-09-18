/**
 * Modal "Gerar Ordem de Trafego": pode ser aberta em branco (botao do
 * cabecalho do calendario) ou pre-preenchida a partir da viagem selecionada
 * no momento. Sempre baixa o PDF gerado pelo backend e abre numa nova aba.
 */
let modalOrdemTrafego = null;
let unidadesOrdemTrafego = [];

document.addEventListener("DOMContentLoaded", async () => {
    modalOrdemTrafego = new bootstrap.Modal(document.getElementById("modal-ordem-trafego"));
    unidadesOrdemTrafego = await Api.get("/api/locais-administrativos");

    document.getElementById("botao-ordem-trafego").addEventListener("click", () => abrirOrdemTrafego());

    const botaoNaViagem = document.getElementById("botao-ordem-trafego-viagem");
    if (botaoNaViagem) {
        botaoNaViagem.addEventListener("click", () => abrirOrdemTrafego(viagemSelecionadaAtual));
    }

    document.getElementById("form-ordem-trafego").addEventListener("submit", gerarOrdemTrafegoPdf);
});

function abrirOrdemTrafego(viagem) {
    document.getElementById("mensagem-erro-ordem").classList.add("d-none");
    document.getElementById("form-ordem-trafego").reset();

    montarOpcoesSelect("select-ordem-veiculo", veiculosCarregados, (veiculo) => `${veiculo.placa} - ${veiculo.modelo}`);
    montarOpcoesSelect("select-ordem-motorista", motoristasCarregados, (motorista) => motorista.nome);
    montarOpcoesSelect("select-ordem-unidade", unidadesOrdemTrafego, (unidade) => unidade.nome, "nome");

    if (viagem) {
        document.getElementById("select-ordem-veiculo").value = viagem.veiculoId;
        document.getElementById("select-ordem-motorista").value = viagem.motoristaId;
        document.getElementById("input-ordem-data").value = viagem.dataHoraSaida.substring(0, 10);
        document.getElementById("input-ordem-data-fim").value = viagem.dataHoraChegadaEstimada
            ? viagem.dataHoraChegadaEstimada.substring(0, 10)
            : "";
        if (viagem.unidadeOrigem) {
            document.getElementById("select-ordem-unidade").value = viagem.unidadeOrigem;
        }
    }

    modalOrdemTrafego.show();
}

function montarOpcoesSelect(idSelect, itens, obterTexto, obterValor) {
    const select = document.getElementById(idSelect);
    select.innerHTML = "";
    itens
        .map((item) => new Option(obterTexto(item), obterValor ? item[obterValor] : item.id))
        .forEach((opcao) => select.add(opcao));
}

async function gerarOrdemTrafegoPdf(evento) {
    evento.preventDefault();
    const mensagemErro = document.getElementById("mensagem-erro-ordem");
    mensagemErro.classList.add("d-none");

    const corpo = {
        veiculoId: Number(document.getElementById("select-ordem-veiculo").value),
        motoristaId: Number(document.getElementById("select-ordem-motorista").value),
        data: document.getElementById("input-ordem-data").value,
        dataFim: document.getElementById("input-ordem-data-fim").value || null,
        unidade: document.getElementById("select-ordem-unidade").value,
    };

    try {
        const resposta = await fetch("/api/ordens-trafego/pdf", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "same-origin",
            body: JSON.stringify(corpo),
        });
        if (!resposta.ok) {
            const erro = await resposta.json().catch(() => ({ mensagem: "Erro ao gerar a ordem de trafego" }));
            throw new Error(erro.mensagem || "Erro ao gerar a ordem de trafego");
        }

        const blob = await resposta.blob();
        window.open(URL.createObjectURL(blob), "_blank");
        modalOrdemTrafego.hide();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}
