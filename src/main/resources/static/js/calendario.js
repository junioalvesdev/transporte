/**
 * Calendario do Departamento de Transportes — visual inspirado no calendario
 * do sistema legado (grade de mes estilo Google Calendar, com "pilulas" de
 * evento coloridas por status). Visao de mes e a principal; semana e um
 * detalhamento de um dia especifico.
 */
const DIAS_SEMANA = ["Domingo", "Segunda", "Terca", "Quarta", "Quinta", "Sexta", "Sabado"];
const DIAS_SEMANA_ABREV = ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab"];
const MESES = ["Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"];

const classePorStatus = {
    PLANEJADA: "cor-planejada",
    ABERTA_PARA_APROVEITAMENTO: "cor-aberta",
    LOTADA: "cor-lotada",
    EM_ANDAMENTO: "cor-andamento",
    CONCLUIDA: "cor-concluida",
    CANCELADA: "cor-cancelada",
};

const TODOS_STATUS_VIAGEM = Object.keys(classePorStatus);
const MAX_EVENTOS_POR_CELULA_MES = 3;

let modoAtual = "mes";
let dataReferencia = new Date();
let modalDetalheViagem = null;
let modalEventosDia = null;
let viagemSelecionadaId = null;
let viagemSelecionadaAtual = null;
let veiculosCarregados = [];
let motoristasCarregados = [];
let seletorSaidaEdicao = null;
let seletorRetornoEdicao = null;

document.addEventListener("DOMContentLoaded", async () => {
    montarCabecalhosDias();

    document.getElementById("botao-semana").addEventListener("click", () => alternarModo("semana"));
    document.getElementById("botao-mes").addEventListener("click", () => alternarModo("mes"));
    document.getElementById("botao-anterior").addEventListener("click", () => navegar(-1));
    document.getElementById("botao-proximo").addEventListener("click", () => navegar(1));
    document.getElementById("botao-hoje").addEventListener("click", () => {
        dataReferencia = new Date();
        renderizar();
    });

    modalDetalheViagem = new bootstrap.Modal(document.getElementById("modal-detalhe-viagem"));
    modalEventosDia = new bootstrap.Modal(document.getElementById("modal-eventos-dia"));
    seletorSaidaEdicao = montarSeletorDataHora("campo-input-editar-saida", "Saida", true);
    seletorRetornoEdicao = montarSeletorDataHora("campo-input-editar-retorno", "Retorno estimado", false);
    document.getElementById("botao-salvar-status").addEventListener("click", salvarNovoStatus);
    document.getElementById("form-detalhe-viagem").addEventListener("submit", salvarDetalhesViagem);

    await Promise.all([carregarVeiculos(), carregarMotoristas()]);
    renderizar();
});

async function carregarVeiculos() {
    const resultado = await Api.get("/api/veiculos?pagina=0&tamanho=1000");
    veiculosCarregados = resultado.conteudo;
}

async function carregarMotoristas() {
    const resultado = await Api.get("/api/motoristas?pagina=0&tamanho=1000");
    motoristasCarregados = resultado.conteudo;
}

function montarCabecalhosDias() {
    ["mes-cabecalhos", "semana-cabecalhos"].forEach((id) => {
        const alvo = document.getElementById(id);
        DIAS_SEMANA_ABREV
            .map((nome) => { const div = document.createElement("div"); div.className = "mes-dia-semana"; div.textContent = nome; return div; })
            .forEach((div) => alvo.appendChild(div));
    });
}

function alternarModo(modo) {
    modoAtual = modo;
    document.getElementById("visao-semana").classList.toggle("d-none", modo !== "semana");
    document.getElementById("visao-mes").classList.toggle("d-none", modo !== "mes");
    document.getElementById("botao-semana").classList.toggle("ativo", modo === "semana");
    document.getElementById("botao-mes").classList.toggle("ativo", modo === "mes");
    renderizar();
}

function navegar(direcao) {
    if (modoAtual === "semana") {
        dataReferencia.setDate(dataReferencia.getDate() + direcao * 7);
    } else {
        dataReferencia.setMonth(dataReferencia.getMonth() + direcao);
    }
    renderizar();
}

function inicioDaSemana(data) {
    const copia = new Date(data);
    copia.setDate(copia.getDate() - copia.getDay());
    copia.setHours(0, 0, 0, 0);
    return copia;
}

async function renderizar() {
    if (modoAtual === "semana") {
        await renderizarSemana();
    } else {
        await renderizarMes();
    }
}

async function renderizarSemana() {
    const inicio = inicioDaSemana(dataReferencia);
    const fim = new Date(inicio);
    fim.setDate(fim.getDate() + 7);

    document.getElementById("titulo-periodo").textContent =
        `${inicio.toLocaleDateString("pt-BR")} — ${new Date(fim - 1).toLocaleDateString("pt-BR")}`;

    const viagens = await buscarViagens(inicio, fim);
    const grade = document.getElementById("visao-semana-grade");
    grade.innerHTML = "";

    Array.from({ length: 7 }, (_, indiceDia) => {
        const dia = new Date(inicio);
        dia.setDate(dia.getDate() + indiceDia);
        return dia;
    })
        .map((dia) => criarColunaSemana(dia, viagens))
        .forEach((coluna) => grade.appendChild(coluna));
}

function criarColunaSemana(dia, viagens) {
    const ehHoje = mesmoDia(dia, new Date());
    const viagensDoDia = viagens
        .filter((v) => mesmoDia(new Date(v.dataHoraSaida), dia))
        .sort((a, b) => new Date(a.dataHoraSaida) - new Date(b.dataHoraSaida));

    const coluna = document.createElement("div");
    coluna.className = "semana-coluna";
    coluna.innerHTML = `<div class="semana-cabecalho ${ehHoje ? "hoje" : ""}">${dia.getDate()}</div>`;

    viagensDoDia.map((viagem) => criarPilulaEvento(viagem, true)).forEach((pilula) => coluna.appendChild(pilula));
    return coluna;
}

function criarPilulaEvento(viagem, detalhado = false) {
    const hora = new Date(viagem.dataHoraSaida).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
    const pilula = document.createElement("div");
    pilula.className = `evento-pilula ${classePorStatus[viagem.status] || "cor-planejada"}`;
    pilula.textContent = detalhado
        ? `${hora} · ${viagem.veiculoPlaca} · ${viagem.motoristaNome}`
        : `${hora} ${viagem.veiculoPlaca}`;
    pilula.title = `${viagem.veiculoPlaca} — ${viagem.motoristaNome} (${viagem.vagasOcupadas}/${viagem.capacidadeTotal})`;
    pilula.addEventListener("click", (evento) => {
        evento.stopPropagation();
        abrirDetalheViagem(viagem);
    });
    return pilula;
}

async function renderizarMes() {
    const ano = dataReferencia.getFullYear();
    const mes = dataReferencia.getMonth();
    const primeiroDiaDoMes = new Date(ano, mes, 1);
    const inicioGrade = inicioDaSemana(primeiroDiaDoMes);
    const fimGrade = new Date(inicioGrade);
    fimGrade.setDate(fimGrade.getDate() + 42);

    document.getElementById("titulo-periodo").textContent = `${MESES[mes]} de ${ano}`;

    const viagens = await buscarViagens(inicioGrade, fimGrade);
    const grade = document.getElementById("visao-mes-grade");
    grade.innerHTML = "";

    Array.from({ length: 42 }, (_, indice) => {
        const dia = new Date(inicioGrade);
        dia.setDate(dia.getDate() + indice);
        return dia;
    })
        .map((dia) => criarCelulaMes(dia, mes, viagens))
        .forEach((celula) => grade.appendChild(celula));
}

function criarCelulaMes(dia, mesAtual, viagens) {
    const ehHoje = mesmoDia(dia, new Date());
    const viagensDoDia = viagens
        .filter((v) => mesmoDia(new Date(v.dataHoraSaida), dia))
        .sort((a, b) => new Date(a.dataHoraSaida) - new Date(b.dataHoraSaida));

    const celula = document.createElement("div");
    celula.className = `mes-celula ${dia.getMonth() !== mesAtual ? "fora-do-mes" : ""} ${ehHoje ? "hoje" : ""}`;
    celula.innerHTML = `<div class="mes-dia-numero">${dia.getDate()}</div>`;

    viagensDoDia
        .slice(0, MAX_EVENTOS_POR_CELULA_MES)
        .map((viagem) => criarPilulaEvento(viagem))
        .forEach((pilula) => celula.appendChild(pilula));

    const restantes = viagensDoDia.length - MAX_EVENTOS_POR_CELULA_MES;
    if (restantes > 0) {
        const maisEventos = document.createElement("div");
        maisEventos.className = "mais-eventos";
        maisEventos.textContent = `+${restantes} mais`;
        maisEventos.addEventListener("click", (evento) => {
            evento.stopPropagation();
            abrirEventosDoDia(dia, viagensDoDia);
        });
        celula.appendChild(maisEventos);
    }

    // clicar em qualquer parte vazia do dia tambem leva pra semana daquele dia
    celula.addEventListener("click", () => {
        dataReferencia = new Date(dia);
        alternarModo("semana");
    });

    return celula;
}

function abrirEventosDoDia(dia, viagensDoDia) {
    document.getElementById("titulo-eventos-dia").textContent = `Viagens de ${dia.toLocaleDateString("pt-BR")}`;
    const lista = document.getElementById("lista-eventos-dia");
    lista.innerHTML = "";
    viagensDoDia.forEach((viagem) => {
        const hora = new Date(viagem.dataHoraSaida).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
        const linha = document.createElement("div");
        linha.className = `evento-pilula ${classePorStatus[viagem.status] || "cor-planejada"}`;
        linha.style.whiteSpace = "normal";
        linha.textContent = `${hora} · ${viagem.veiculoPlaca} · ${viagem.motoristaNome} (${viagem.vagasOcupadas}/${viagem.capacidadeTotal})`;
        linha.addEventListener("click", () => {
            modalEventosDia.hide();
            abrirDetalheViagem(viagem);
        });
        lista.appendChild(linha);
    });
    modalEventosDia.show();
}

function viagemJaPassou(viagem) {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const dataViagem = new Date(viagem.dataHoraSaida);
    dataViagem.setHours(0, 0, 0, 0);
    return dataViagem < hoje;
}

function abrirDetalheViagem(viagem) {
    viagemSelecionadaId = viagem.id;
    viagemSelecionadaAtual = viagem;
    document.getElementById("detalhe-solicitantes").textContent = viagem.solicitantesNomes.length > 0
        ? viagem.solicitantesNomes.join(", ")
        : "-";
    document.getElementById("detalhe-status").innerHTML = `<span class="evento-pilula ${classePorStatus[viagem.status] || "cor-planejada"}">${viagem.status}</span>`;
    document.getElementById("mensagem-erro-status").classList.add("d-none");
    document.getElementById("mensagem-erro-detalhe").classList.add("d-none");

    const jaPassou = viagemJaPassou(viagem);
    document.getElementById("detalhe-viagem-passada").classList.toggle("d-none", !jaPassou);
    document.getElementById("form-detalhe-viagem").classList.toggle("d-none", jaPassou);
    document.getElementById("secao-status").classList.toggle("d-none", jaPassou);

    if (!jaPassou) {
        montarFormularioEdicao(viagem);
    }

    modalDetalheViagem.show();
}

function montarFormularioEdicao(viagem) {
    const selectVeiculo = document.getElementById("select-editar-veiculo");
    selectVeiculo.innerHTML = "";
    veiculosCarregados
        .map((veiculo) => new Option(`${veiculo.placa} - ${veiculo.modelo} (${veiculo.capacidade} lugares)`, veiculo.id, false, veiculo.id === viagem.veiculoId))
        .forEach((opcao) => selectVeiculo.add(opcao));
    selectVeiculo.value = viagem.veiculoId;

    const selectMotorista = document.getElementById("select-editar-motorista");
    selectMotorista.innerHTML = "";
    motoristasCarregados
        .map((motorista) => new Option(motorista.nome, motorista.id, false, motorista.id === viagem.motoristaId))
        .forEach((opcao) => selectMotorista.add(opcao));
    selectMotorista.value = viagem.motoristaId;

    seletorSaidaEdicao.definirValor(viagem.dataHoraSaida.substring(0, 16));
    seletorRetornoEdicao.definirValor(viagem.dataHoraChegadaEstimada
        ? viagem.dataHoraChegadaEstimada.substring(0, 16)
        : "");
    document.getElementById("input-editar-capacidade").value = viagem.capacidadeTotal;
    document.getElementById("input-editar-capacidade").min = viagem.vagasOcupadas || 1;
    document.getElementById("detalhe-ocupacao-atual").textContent = `${viagem.vagasOcupadas}/${viagem.capacidadeTotal}`;

    const selectStatus = document.getElementById("select-novo-status");
    selectStatus.innerHTML = "";
    TODOS_STATUS_VIAGEM
        .filter((status) => status !== viagem.status)
        .map((status) => new Option(status, status))
        .forEach((opcao) => selectStatus.add(opcao));
}

async function salvarDetalhesViagem(evento) {
    evento.preventDefault();
    const mensagemErro = document.getElementById("mensagem-erro-detalhe");
    mensagemErro.classList.add("d-none");

    const corpo = {
        veiculoId: Number(document.getElementById("select-editar-veiculo").value),
        motoristaId: Number(document.getElementById("select-editar-motorista").value),
        dataHoraSaida: seletorSaidaEdicao.obterValor(),
        dataHoraChegadaEstimada: seletorRetornoEdicao.obterValor() || null,
        capacidadeTotal: Number(document.getElementById("input-editar-capacidade").value),
    };

    try {
        await Api.patch(`/api/viagens/${viagemSelecionadaId}`, corpo);
        modalDetalheViagem.hide();
        Ui.mostrarToast("Viagem atualizada.");
        await renderizar();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

async function salvarNovoStatus() {
    const mensagemErro = document.getElementById("mensagem-erro-status");
    mensagemErro.classList.add("d-none");
    const novoStatus = document.getElementById("select-novo-status").value;

    try {
        await Api.patch(`/api/viagens/${viagemSelecionadaId}/status?novoStatus=${novoStatus}`);
        modalDetalheViagem.hide();
        Ui.mostrarToast("Status da viagem atualizado.");
        await renderizar();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

function mesmoDia(a, b) {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

async function buscarViagens(inicio, fim) {
    const parametros = new URLSearchParams({
        inicio: formatarDataHoraLocal(inicio),
        fim: formatarDataHoraLocal(fim),
    });
    return Api.get(`/api/viagens/calendario?${parametros.toString()}`);
}

function formatarDataHoraLocal(data) {
    const pad = (numero) => String(numero).padStart(2, "0");
    return `${data.getFullYear()}-${pad(data.getMonth() + 1)}-${pad(data.getDate())}T${pad(data.getHours())}:${pad(data.getMinutes())}:00`;
}
