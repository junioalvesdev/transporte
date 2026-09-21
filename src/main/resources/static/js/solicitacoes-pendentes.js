/**
 * Tela do Departamento de Transportes para decidir solicitacoes pendentes:
 * aprovar (criando uma viagem nova com rota origem -> destino simples e
 * vinculando a solicitacao) ou reprovar (com motivo obrigatorio).
 */
let solicitacaoSelecionada = null;
let modalAprovar = null;
let modalReprovar = null;
let veiculosCarregados = [];
let seletorSaidaAprovar = null;
let seletorChegadaAprovar = null;
let paginaAtualSolicitacoes = 0;

const INTERVALO_AUTO_REFRESH_MS = 20000;

document.addEventListener("DOMContentLoaded", async () => {
    modalAprovar = new bootstrap.Modal(document.getElementById("modal-aprovar"));
    modalReprovar = new bootstrap.Modal(document.getElementById("modal-reprovar"));
    seletorSaidaAprovar = montarSeletorDataHora("campo-input-saida", "Saida", true);
    seletorChegadaAprovar = montarSeletorDataHora("campo-input-chegada", "Retorno estimado", true);
    await Promise.all([carregarSolicitacoes(), carregarVeiculos(), carregarMotoristas(), carregarTiposVeiculo()]);

    document.getElementById("form-aprovar").addEventListener("submit", confirmarAprovacao);
    document.getElementById("form-reprovar").addEventListener("submit", confirmarReprovacao);
    document.getElementById("select-tipo-veiculo-aprovar").addEventListener("change", filtrarVeiculosPorTipo);

    // atualiza sozinho de tempos em tempos, mas nunca enquanto algum modal
    // estiver aberto — se o admin esta no meio de aprovar/reprovar, um
    // refresh no fundo trocaria a lista debaixo dele e atrapalharia
    setInterval(() => {
        if (document.querySelector(".modal.show")) {
            return;
        }
        carregarSolicitacoes(paginaAtualSolicitacoes);
    }, INTERVALO_AUTO_REFRESH_MS);
});

async function carregarSolicitacoes(pagina = 0) {
    paginaAtualSolicitacoes = pagina;
    const resultado = await Api.get(`/api/solicitacoes?status=PENDENTE&pagina=${pagina}&tamanho=10`);
    const lista = document.getElementById("lista-solicitacoes");
    lista.innerHTML = "";
    document.getElementById("mensagem-vazio").classList.toggle("d-none", resultado.conteudo.length > 0);

    resultado.conteudo.map(criarCartao).forEach((cartao) => lista.appendChild(cartao));
    Ui.montarPaginacao(document.getElementById("paginacao-solicitacoes"), resultado, carregarSolicitacoes);
}

function criarCartao(solicitacao) {
    const dataHora = new Date(solicitacao.dataHoraDesejada);

    const cartao = document.createElement("div");
    cartao.className = "cartao cartao-viagem d-flex justify-content-between align-items-center flex-wrap gap-3";
    cartao.innerHTML = `
        <div>
            <div class="rota-titulo">${solicitacao.origemExibicao} &rarr; ${solicitacao.destinoExibicao}</div>
            <div class="text-muted small">
                ${dataHora.toLocaleString("pt-BR")} &middot; ${solicitacao.qtdPassageiros} passageiro(s)
                ${solicitacao.urgente ? '<span class="badge bg-danger ms-1">Urgente</span>' : ""}
            </div>
            <div class="text-muted small"><i class="bi bi-person-fill me-1"></i>${solicitacao.solicitanteNome}</div>
            ${solicitacao.telefoneContato ? `<div class="text-muted small"><i class="bi bi-telephone-fill me-1"></i>${solicitacao.telefoneContato}</div>` : ""}
            ${solicitacao.finalidade ? `<div class="mt-1">${solicitacao.finalidade}</div>` : ""}
        </div>
        <div class="d-flex gap-2">
            <button class="btn btn-ovg-secundario btn-sm" data-acao="reprovar">Reprovar</button>
            <button class="btn btn-ovg-principal btn-sm" data-acao="aprovar">Aprovar</button>
        </div>
    `;

    cartao.querySelector('[data-acao="aprovar"]').addEventListener("click", () => abrirModalAprovar(solicitacao));
    cartao.querySelector('[data-acao="reprovar"]').addEventListener("click", () => abrirModalReprovar(solicitacao));
    return cartao;
}

function abrirModalAprovar(solicitacao) {
    solicitacaoSelecionada = solicitacao;
    document.getElementById("mensagem-erro-aprovar").classList.add("d-none");
    seletorSaidaAprovar.definirValor(solicitacao.dataHoraDesejada.substring(0, 16));
    seletorChegadaAprovar.definirValor(solicitacao.dataHoraRetornoDesejada
        ? solicitacao.dataHoraRetornoDesejada.substring(0, 16)
        : "");
    document.getElementById("select-tipo-veiculo-aprovar").value = "";
    montarOpcoesVeiculo(veiculosCarregados);
    modalAprovar.show();
}

async function confirmarAprovacao(evento) {
    evento.preventDefault();
    const mensagemErro = document.getElementById("mensagem-erro-aprovar");
    mensagemErro.classList.add("d-none");

    try {
        await Api.patch(`/api/solicitacoes/${solicitacaoSelecionada.id}/aprovar`, {
            novaViagem: {
                veiculoId: Number(document.getElementById("select-veiculo").value),
                motoristaId: Number(document.getElementById("select-motorista").value),
                dataHoraSaida: seletorSaidaAprovar.obterValor(),
                dataHoraChegadaEstimada: seletorChegadaAprovar.obterValor(),
                pontos: [
                    { localId: solicitacaoSelecionada.localOrigemId, cidadeId: solicitacaoSelecionada.cidadeOrigemId, ordem: 1, tipoPonto: "ORIGEM" },
                    { localId: solicitacaoSelecionada.localDestinoId, cidadeId: solicitacaoSelecionada.cidadeDestinoId, ordem: 2, tipoPonto: "DESTINO" },
                ],
            },
            cidadeEmbarqueId: solicitacaoSelecionada.cidadeOrigemId,
            cidadeDesembarqueId: solicitacaoSelecionada.cidadeDestinoId,
        });
        modalAprovar.hide();
        Ui.mostrarToast("Solicitacao aprovada e viagem criada.");
        await carregarSolicitacoes();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

function abrirModalReprovar(solicitacao) {
    solicitacaoSelecionada = solicitacao;
    document.getElementById("form-reprovar").reset();
    document.getElementById("mensagem-erro-reprovar").classList.add("d-none");
    modalReprovar.show();
}

async function confirmarReprovacao(evento) {
    evento.preventDefault();
    const mensagemErro = document.getElementById("mensagem-erro-reprovar");
    mensagemErro.classList.add("d-none");

    try {
        await Api.patch(`/api/solicitacoes/${solicitacaoSelecionada.id}/reprovar`, {
            motivoRecusa: document.getElementById("input-motivo-recusa").value,
        });
        modalReprovar.hide();
        Ui.mostrarToast("Solicitacao reprovada.");
        await carregarSolicitacoes();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

async function carregarVeiculos() {
    // select de aprovacao precisa de todos os veiculos ativos, nao so uma pagina
    const resultado = await Api.get("/api/veiculos?pagina=0&tamanho=1000");
    veiculosCarregados = resultado.conteudo;
    montarOpcoesVeiculo(veiculosCarregados);
}

async function carregarTiposVeiculo() {
    const tipos = await Api.get("/api/tipos-veiculo");
    const select = document.getElementById("select-tipo-veiculo-aprovar");
    select.add(new Option("Todos os tipos", ""));
    tipos.map((tipo) => new Option(tipo.nome, tipo.nome)).forEach((opcao) => select.add(opcao));
}

function filtrarVeiculosPorTipo() {
    const tipoEscolhido = document.getElementById("select-tipo-veiculo-aprovar").value;
    const veiculosFiltrados = tipoEscolhido
        ? veiculosCarregados.filter((veiculo) => veiculo.tipoVeiculo === tipoEscolhido)
        : veiculosCarregados;
    montarOpcoesVeiculo(veiculosFiltrados);
}

function montarOpcoesVeiculo(veiculos) {
    const select = document.getElementById("select-veiculo");
    select.innerHTML = "";
    veiculos
        .map((veiculo) => new Option(`${veiculo.placa} - ${veiculo.modelo} (${veiculo.capacidade} lugares)`, veiculo.id))
        .forEach((opcao) => select.add(opcao));
}

async function carregarMotoristas() {
    // select de aprovacao precisa de todos os motoristas ativos, nao so uma pagina
    const resultado = await Api.get("/api/motoristas?pagina=0&tamanho=1000");
    const select = document.getElementById("select-motorista");
    resultado.conteudo
        .map((motorista) => new Option(motorista.nome, motorista.id))
        .forEach((opcao) => select.add(opcao));
}
