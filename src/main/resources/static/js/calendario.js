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

const rotuloPorStatus = {
    PLANEJADA: "Planejada",
    ABERTA_PARA_APROVEITAMENTO: "Aberta p/ aproveitamento",
    LOTADA: "Lotada",
    EM_ANDAMENTO: "Em andamento",
    CONCLUIDA: "Concluida",
    CANCELADA: "Cancelada",
};

const TODOS_STATUS_VIAGEM = Object.keys(classePorStatus);
const MAX_EVENTOS_POR_CELULA_MES = 3;

// Solicitacoes que ainda nao viraram viagem (sem veiculo/motorista ainda) —
// aparecem no calendario tambem, com pilula propria pra nao confundir com
// viagem ja confirmada. Aprovada/atendida nao entram aqui: ja aparecem
// atraves da propria viagem vinculada.
const classePorStatusSolicitacaoCalendario = {
    PENDENTE: "cor-solicitacao-pendente",
    REPROVADA: "cor-solicitacao-reprovada",
    CANCELADA: "cor-solicitacao-cancelada",
};

const rotuloPorStatusSolicitacaoCalendario = {
    PENDENTE: "Pendente",
    REPROVADA: "Reprovada",
    CANCELADA: "Cancelada",
};

let modoAtual = "mes";
let dataReferencia = new Date();
let statusAtivos = new Set(TODOS_STATUS_VIAGEM);
let modalDetalheViagem = null;
let modalDetalheSolicitacao = null;
let modalEventosDia = null;
let viagemSelecionadaId = null;
let viagemSelecionadaAtual = null;
let solicitacaoSelecionada = null;
let seletorDataEdicaoSolicitacao = null;
let seletorRetornoEdicaoSolicitacao = null;
let veiculosCarregados = [];
let motoristasCarregados = [];
let seletorSaidaEdicao = null;
let seletorRetornoEdicao = null;
let ehTransporte = false;
let loginUsuarioAtual = null;

const INTERVALO_AUTO_REFRESH_MS = 20000;

document.addEventListener("DOMContentLoaded", async () => {
    montarCabecalhosDias();
    montarCabecalhoDiasMini();
    renderizarFiltrosStatus();

    document.getElementById("botao-semana").addEventListener("click", () => alternarModo("semana"));
    document.getElementById("botao-mes").addEventListener("click", () => alternarModo("mes"));
    document.getElementById("botao-anterior").addEventListener("click", () => navegar(-1));
    document.getElementById("botao-proximo").addEventListener("click", () => navegar(1));
    document.getElementById("botao-hoje").addEventListener("click", () => {
        dataReferencia = new Date();
        renderizar();
    });

    renderizarMiniCalendario();

    const perfil = await window.perfilUsuarioPromise;
    ehTransporte = perfil.ehTransporte;
    loginUsuarioAtual = perfil.login;
    // so quem tem perfil TRANSPORTE pode editar viagem/gerar ordem de trafego —
    // sem isso, nem os selects de veiculo/motorista (a API tambem recusa esse
    // GET pra quem nao e TRANSPORTE) nem os botoes de acao fazem sentido
    document.getElementById("botao-ordem-trafego").classList.toggle("d-none", !ehTransporte);

    modalDetalheViagem = new bootstrap.Modal(document.getElementById("modal-detalhe-viagem"));
    modalDetalheSolicitacao = new bootstrap.Modal(document.getElementById("modal-detalhe-solicitacao"));
    modalEventosDia = new bootstrap.Modal(document.getElementById("modal-eventos-dia"));
    seletorSaidaEdicao = montarSeletorDataHora("campo-input-editar-saida", "Saida", true);
    seletorRetornoEdicao = montarSeletorDataHora("campo-input-editar-retorno", "Retorno estimado", false);
    document.getElementById("botao-cancelar-participacao").addEventListener("click", cancelarMinhaParticipacao);
    document.getElementById("form-detalhe-viagem").addEventListener("submit", salvarDetalhesViagem);
    document.getElementById("botao-cancelar-solicitacao").addEventListener("click", cancelarSolicitacaoCalendario);
    document.getElementById("form-editar-solicitacao").addEventListener("submit", salvarEdicaoSolicitacao);

    if (ehTransporte) {
        await Promise.all([carregarVeiculos(), carregarMotoristas()]);
    }

    renderizar();

    // atualiza sozinho de tempos em tempos (mesmo padrao da tela de
    // Solicitacoes pendentes), mas nunca com algum modal aberto — evita trocar
    // o calendario debaixo de quem esta no meio de editar/criar algo
    setInterval(() => {
        if (document.querySelector(".modal.show")) {
            return;
        }
        renderizar();
    }, INTERVALO_AUTO_REFRESH_MS);
});

function montarCabecalhoDiasMini() {
    const alvo = document.getElementById("mini-calendario-dias-semana");
    "DSTQQSS".split("").forEach((letra) => {
        const span = document.createElement("span");
        span.textContent = letra;
        alvo.appendChild(span);
    });
}

function renderizarFiltrosStatus() {
    const alvo = document.getElementById("lista-filtro-status");
    alvo.innerHTML = "";
    TODOS_STATUS_VIAGEM.forEach((status) => {
        const item = document.createElement("label");
        item.className = "filtro-status-item";
        item.innerHTML = `
            <input type="checkbox" checked data-status="${status}">
            <span class="filtro-status-bolinha ${classePorStatus[status]}"></span>
            ${rotuloPorStatus[status]}
        `;
        item.querySelector("input").addEventListener("change", (evento) => {
            if (evento.target.checked) {
                statusAtivos.add(status);
            } else {
                statusAtivos.delete(status);
            }
            renderizar();
        });
        alvo.appendChild(item);
    });
}

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

/**
 * Mini-calendario da lateral: so exibicao, sem navegacao nem clique — sempre
 * mostra o mes atual de verdade, com o dia de hoje marcado, independente da
 * navegacao do calendario principal.
 */
function renderizarMiniCalendario() {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = hoje.getMonth();
    const primeiroDiaDoMes = new Date(ano, mes, 1);
    const inicioGrade = inicioDaSemana(primeiroDiaDoMes);

    document.getElementById("mini-calendario-titulo").textContent = `${MESES[mes]} de ${ano}`;

    const grade = document.getElementById("mini-calendario-grade");
    grade.innerHTML = "";

    Array.from({ length: 42 }, (_, indice) => {
        const dia = new Date(inicioGrade);
        dia.setDate(dia.getDate() + indice);
        return dia;
    }).forEach((dia) => {
        const celula = document.createElement("div");
        celula.className = "mini-dia";
        if (dia.getMonth() !== mes) celula.classList.add("fora-do-mes");
        if (mesmoDia(dia, hoje)) celula.classList.add("hoje");
        celula.textContent = dia.getDate();
        grade.appendChild(celula);
    });
}

async function renderizarSemana() {
    const inicio = inicioDaSemana(dataReferencia);
    const fim = new Date(inicio);
    fim.setDate(fim.getDate() + 7);

    document.getElementById("titulo-periodo").textContent =
        `${inicio.toLocaleDateString("pt-BR")} — ${new Date(fim - 1).toLocaleDateString("pt-BR")}`;

    const eventos = await buscarEventosCalendario(inicio, fim);
    const grade = document.getElementById("visao-semana-grade");
    grade.innerHTML = "";

    Array.from({ length: 7 }, (_, indiceDia) => {
        const dia = new Date(inicio);
        dia.setDate(dia.getDate() + indiceDia);
        return dia;
    })
        .map((dia) => criarColunaSemana(dia, eventos))
        .forEach((coluna) => grade.appendChild(coluna));
}

function criarColunaSemana(dia, eventos) {
    const ehHoje = mesmoDia(dia, new Date());
    const eventosDoDia = eventos
        .filter((e) => mesmoDia(new Date(e._dataReferencia), dia))
        .sort((a, b) => new Date(a._dataReferencia) - new Date(b._dataReferencia));

    const coluna = document.createElement("div");
    coluna.className = "semana-coluna";
    coluna.innerHTML = `<div class="semana-cabecalho ${ehHoje ? "hoje" : ""}">${dia.getDate()}</div>`;

    eventosDoDia.map((evento) => criarPilulaEvento(evento, true)).forEach((pilula) => coluna.appendChild(pilula));
    return coluna;
}

function criarPilulaEvento(evento, detalhado = false) {
    if (evento._tipo === "solicitacao") {
        return criarPilulaSolicitacao(evento);
    }
    const viagem = evento;
    const hora = new Date(viagem.dataHoraSaida).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
    const finalidade = viagem.finalidade || "Sem finalidade informada";
    const pilula = document.createElement("div");
    pilula.className = `evento-pilula ${classePorStatus[viagem.status] || "cor-planejada"}${viagem.urgente ? " urgente" : ""}`;
    pilula.textContent = `${hora} ${finalidade}`;
    pilula.title = `${finalidade} — ${viagem.veiculoPlaca} · ${viagem.motoristaNome} (${viagem.vagasOcupadas}/${viagem.capacidadeTotal})${viagem.urgente ? " · URGENTE" : ""}`;
    pilula.addEventListener("click", (evt) => {
        evt.stopPropagation();
        abrirDetalheViagem(viagem);
    });
    return pilula;
}

function criarPilulaSolicitacao(solicitacao) {
    const hora = new Date(solicitacao.dataHoraDesejada).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
    const finalidade = solicitacao.finalidade || "Sem finalidade informada";
    const rotulo = rotuloPorStatusSolicitacaoCalendario[solicitacao.status] || solicitacao.status;
    const pilula = document.createElement("div");
    pilula.className = `evento-pilula ${classePorStatusSolicitacaoCalendario[solicitacao.status] || "cor-solicitacao-pendente"}${solicitacao.urgente ? " urgente" : ""}`;
    pilula.textContent = `${hora} [${rotulo}] ${finalidade}`;
    pilula.title = `${finalidade} — solicitacao ${rotulo.toLowerCase()}${solicitacao.urgente ? " · URGENTE" : ""}`;
    pilula.addEventListener("click", (evt) => {
        evt.stopPropagation();
        if (modalEventosDia) {
            modalEventosDia.hide();
        }
        abrirDetalheSolicitacao(solicitacao);
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

    const eventos = await buscarEventosCalendario(inicioGrade, fimGrade);
    const grade = document.getElementById("visao-mes-grade");
    grade.innerHTML = "";

    Array.from({ length: 42 }, (_, indice) => {
        const dia = new Date(inicioGrade);
        dia.setDate(dia.getDate() + indice);
        return dia;
    })
        .map((dia) => criarCelulaMes(dia, mes, eventos))
        .forEach((celula) => grade.appendChild(celula));
}

function criarCelulaMes(dia, mesAtual, eventos) {
    const ehHoje = mesmoDia(dia, new Date());
    const eventosDoDia = eventos
        .filter((e) => mesmoDia(new Date(e._dataReferencia), dia))
        .sort((a, b) => new Date(a._dataReferencia) - new Date(b._dataReferencia));

    const celula = document.createElement("div");
    celula.className = `mes-celula ${dia.getMonth() !== mesAtual ? "fora-do-mes" : ""} ${ehHoje ? "hoje" : ""}`;
    celula.innerHTML = `<div class="mes-dia-numero">${dia.getDate()}</div>`;

    eventosDoDia
        .slice(0, MAX_EVENTOS_POR_CELULA_MES)
        .map((evento) => criarPilulaEvento(evento))
        .forEach((pilula) => celula.appendChild(pilula));

    const restantes = eventosDoDia.length - MAX_EVENTOS_POR_CELULA_MES;
    if (restantes > 0) {
        const maisEventos = document.createElement("div");
        maisEventos.className = "mais-eventos";
        maisEventos.textContent = `+${restantes} mais`;
        maisEventos.addEventListener("click", (evt) => {
            evt.stopPropagation();
            abrirEventosDoDia(dia, eventosDoDia);
        });
        celula.appendChild(maisEventos);
    }

    return celula;
}

function abrirEventosDoDia(dia, eventosDoDia) {
    document.getElementById("titulo-eventos-dia").textContent = `Viagens de ${dia.toLocaleDateString("pt-BR")}`;
    const lista = document.getElementById("lista-eventos-dia");
    lista.innerHTML = "";
    eventosDoDia.forEach((evento) => {
        if (evento._tipo === "solicitacao") {
            const pilula = criarPilulaSolicitacao(evento);
            pilula.style.whiteSpace = "normal";
            lista.appendChild(pilula);
            return;
        }
        const viagem = evento;
        const hora = new Date(viagem.dataHoraSaida).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
        const linha = document.createElement("div");
        linha.className = `evento-pilula ${classePorStatus[viagem.status] || "cor-planejada"}${viagem.urgente ? " urgente" : ""}`;
        linha.style.whiteSpace = "normal";
        linha.textContent = `${hora} · ${viagem.veiculoPlaca} · ${viagem.motoristaNome} (${viagem.vagasOcupadas}/${viagem.capacidadeTotal})${viagem.urgente ? " · URGENTE" : ""}`;
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

    const temTelefone = viagem.telefonesContato && viagem.telefonesContato.length > 0;
    document.getElementById("detalhe-telefone-rotulo").classList.toggle("d-none", !temTelefone);
    document.getElementById("detalhe-telefone").classList.toggle("d-none", !temTelefone);
    document.getElementById("detalhe-telefone").textContent = temTelefone ? viagem.telefonesContato.join(", ") : "";

    const temTitulo = Boolean(viagem.finalidade);
    document.getElementById("detalhe-titulo-rotulo").classList.toggle("d-none", !temTitulo);
    document.getElementById("detalhe-titulo").classList.toggle("d-none", !temTitulo);
    document.getElementById("detalhe-titulo").textContent = viagem.finalidade || "";

    document.getElementById("detalhe-origem").textContent = viagem.origemExibicao || "-";
    document.getElementById("detalhe-destino").textContent = viagem.destinoExibicao || "-";
    document.getElementById("detalhe-saida").textContent = new Date(viagem.dataHoraSaida).toLocaleString("pt-BR");
    document.getElementById("detalhe-retorno").textContent = viagem.dataHoraChegadaEstimada
        ? new Date(viagem.dataHoraChegadaEstimada).toLocaleString("pt-BR")
        : "-";
    document.getElementById("detalhe-ocupacao-leitura").textContent = `${viagem.vagasOcupadas}/${viagem.capacidadeTotal}`;
    document.getElementById("detalhe-veiculo").textContent = viagem.veiculoPlaca;
    document.getElementById("detalhe-motorista").textContent = viagem.motoristaNome;
    document.getElementById("detalhe-status").innerHTML = `<span class="evento-pilula ${classePorStatus[viagem.status] || "cor-planejada"}">${viagem.status}</span>`;

    const temObservacoes = viagem.observacoes && viagem.observacoes.length > 0;
    document.getElementById("detalhe-observacoes-rotulo").classList.toggle("d-none", !temObservacoes);
    document.getElementById("detalhe-observacoes").classList.toggle("d-none", !temObservacoes);
    document.getElementById("detalhe-observacoes").textContent = temObservacoes ? viagem.observacoes.join(" | ") : "";

    document.getElementById("mensagem-erro-cancelar").classList.add("d-none");
    document.getElementById("mensagem-erro-detalhe").classList.add("d-none");

    // quem nao e do Departamento de Transportes (ou a viagem ja passou) so ve
    // o resumo acima; TRANSPORTE numa viagem de hoje pra frente ganha tambem o
    // formulario editavel logo abaixo, sem precisar clicar em nada — e quem
    // nao e TRANSPORTE ganha "Cancelar minha participacao" quando tiver uma
    // solicitacao propria
    const jaPassou = viagemJaPassou(viagem);
    const podeEditar = ehTransporte && !jaPassou;
    const podeCancelarParticipacao = !ehTransporte && !jaPassou && viagem.minhaSolicitacaoId;

    document.getElementById("detalhe-viagem-passada").classList.toggle("d-none", !jaPassou);
    document.getElementById("form-detalhe-viagem").classList.toggle("d-none", !podeEditar);
    document.getElementById("botao-salvar-viagem").classList.toggle("d-none", !podeEditar);
    document.getElementById("botao-cancelar-participacao").classList.toggle("d-none", !podeCancelarParticipacao);

    if (podeEditar) {
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

    // o status atual entra como opcao tambem (pre-selecionado): so vira um
    // PATCH de status a parte se a pessoa realmente escolher outro
    const selectStatus = document.getElementById("select-novo-status");
    selectStatus.innerHTML = "";
    TODOS_STATUS_VIAGEM
        .map((status) => new Option(status, status, false, status === viagem.status))
        .forEach((opcao) => selectStatus.add(opcao));
    selectStatus.value = viagem.status;
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
    const novoStatus = document.getElementById("select-novo-status").value;

    try {
        await Api.patch(`/api/viagens/${viagemSelecionadaId}`, corpo);
        // status e uma transicao a parte (com regras proprias de quem pode ir
        // pra onde) — so dispara se a pessoa realmente trocou o valor
        if (novoStatus !== viagemSelecionadaAtual.status) {
            await Api.patch(`/api/viagens/${viagemSelecionadaId}/status?novoStatus=${novoStatus}`);
        }
        modalDetalheViagem.hide();
        Ui.mostrarToast("Viagem atualizada.");
        await renderizar();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

async function cancelarMinhaParticipacao() {
    const mensagemErro = document.getElementById("mensagem-erro-cancelar");
    mensagemErro.classList.add("d-none");

    const confirmado = await Ui.confirmarAcao("Cancelar sua participacao nesta viagem?", "Cancelar participacao");
    if (!confirmado) {
        return;
    }

    try {
        await Api.patch(`/api/solicitacoes/${viagemSelecionadaAtual.minhaSolicitacaoId}/cancelar`);
        modalDetalheViagem.hide();
        Ui.mostrarToast("Sua participacao foi cancelada.");
        await renderizar();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

// Detalhe de uma solicitacao ainda sem viagem vinculada (pendente, reprovada
// ou cancelada) — mais simples que o de viagem, ja que nao tem veiculo,
// motorista nem ocupacao ainda.
function abrirDetalheSolicitacao(solicitacao) {
    solicitacaoSelecionada = solicitacao;
    document.getElementById("detalhe-sol-solicitante").textContent = solicitacao.solicitanteNome || "-";
    document.getElementById("detalhe-sol-status").innerHTML =
        `<span class="evento-pilula ${classePorStatusSolicitacaoCalendario[solicitacao.status] || "cor-solicitacao-pendente"}">${rotuloPorStatusSolicitacaoCalendario[solicitacao.status] || solicitacao.status}</span>`;
    document.getElementById("detalhe-sol-origem").textContent = solicitacao.origemExibicao || "-";
    document.getElementById("detalhe-sol-destino").textContent = solicitacao.destinoExibicao || "-";
    document.getElementById("detalhe-sol-data").textContent = new Date(solicitacao.dataHoraDesejada).toLocaleString("pt-BR");
    document.getElementById("detalhe-sol-retorno").textContent = solicitacao.dataHoraRetornoDesejada
        ? new Date(solicitacao.dataHoraRetornoDesejada).toLocaleString("pt-BR")
        : "-";

    const temTitulo = Boolean(solicitacao.finalidade);
    document.getElementById("detalhe-sol-titulo-rotulo").classList.toggle("d-none", !temTitulo);
    document.getElementById("detalhe-sol-titulo").classList.toggle("d-none", !temTitulo);
    document.getElementById("detalhe-sol-titulo").textContent = solicitacao.finalidade || "";

    const temMotivo = Boolean(solicitacao.motivoRecusa);
    document.getElementById("detalhe-sol-motivo-rotulo").classList.toggle("d-none", !temMotivo);
    document.getElementById("detalhe-sol-motivo").classList.toggle("d-none", !temMotivo);
    document.getElementById("detalhe-sol-motivo").textContent = solicitacao.motivoRecusa || "";

    // pendente pode ser editada por quem chega a ver essa solicitacao aqui
    // (dono ou TRANSPORTE — o backend so devolve as suas pra quem nao e
    // TRANSPORTE), mas cancelar e so o proprio solicitante (ver
    // SolicitacaoService.cancelar) — TRANSPORTE usa "Reprovar" pra recusar
    const podeEditar = solicitacao.status === "PENDENTE";
    const souODono = solicitacao.solicitanteLogin === loginUsuarioAtual;
    document.getElementById("botao-salvar-solicitacao").classList.toggle("d-none", !podeEditar);
    document.getElementById("botao-cancelar-solicitacao").classList.toggle("d-none", !(podeEditar && souODono));
    document.getElementById("mensagem-erro-cancelar-solicitacao").classList.add("d-none");
    document.getElementById("form-editar-solicitacao").classList.toggle("d-none", !podeEditar);
    document.getElementById("mensagem-erro-editar-solicitacao").classList.add("d-none");

    if (podeEditar) {
        montarFormularioEdicaoSolicitacao(solicitacao);
    }

    modalDetalheSolicitacao.show();
}

async function montarFormularioEdicaoSolicitacao(solicitacao) {
    document.getElementById("input-editar-sol-finalidade").value = solicitacao.finalidade || "";
    document.getElementById("input-editar-sol-passageiros").value = solicitacao.qtdPassageiros;
    document.getElementById("input-editar-sol-telefone").value = solicitacao.telefoneContato || "";
    document.getElementById("input-editar-sol-observacoes").value = solicitacao.observacoes || "";

    await montarSeletorLocal("sol-origem", "Origem", document.getElementById("seletor-sol-origem"));
    await montarSeletorLocal("sol-destino", "Destino", document.getElementById("seletor-sol-destino"));
    preencherSeletorLocal("seletor-sol-origem", "sol-origem", solicitacao.localOrigemId, solicitacao.cidadeOrigemId, solicitacao.descricaoOrigem);
    preencherSeletorLocal("seletor-sol-destino", "sol-destino", solicitacao.localDestinoId, solicitacao.cidadeDestinoId, solicitacao.descricaoDestino);

    seletorDataEdicaoSolicitacao = montarSeletorDataHora("campo-editar-sol-data", "Data e horario desejados", true);
    seletorRetornoEdicaoSolicitacao = montarSeletorDataHora("campo-editar-sol-retorno", "Retorno estimado", true);
    seletorDataEdicaoSolicitacao.definirValor(solicitacao.dataHoraDesejada.substring(0, 16));
    seletorRetornoEdicaoSolicitacao.definirValor(solicitacao.dataHoraRetornoDesejada ? solicitacao.dataHoraRetornoDesejada.substring(0, 16) : "");
}

// preenche o componente de origem/destino (montado do zero pelo
// montarSeletorLocal) com o valor que a solicitacao ja tinha — o componente
// so sabe criar comecando vazio/na primeira unidade, entao ajustamos os
// campos escondidos e o modo (unidade OU "outro endereco") na mao aqui
function preencherSeletorLocal(idContainer, prefixo, localId, cidadeId, descricao) {
    const container = document.getElementById(idContainer);
    const selectUnidade = container.querySelector('[data-campo="select-unidade"]');
    const blocoOutro = container.querySelector('[data-bloco="outro"]');
    const campoTexto = container.querySelector('[data-campo="texto-cidade"]');

    if (localId) {
        selectUnidade.value = String(localId);
        selectUnidade.classList.remove("d-none");
        blocoOutro.classList.add("d-none");
        campoTexto.required = false;
        document.getElementById(`${prefixo}-local-id`).value = localId;
        document.getElementById(`${prefixo}-cidade-id`).value = "";
        document.getElementById(`${prefixo}-descricao`).value = "";
    } else {
        selectUnidade.classList.add("d-none");
        blocoOutro.classList.remove("d-none");
        // ja veio com uma cidade valida (campo escondido abaixo) — o campo de
        // busca visivel e so pra quem esta ESCOLHENDO uma cidade nova, entao
        // nao pode ser obrigatorio aqui, senao uma solicitacao sem descricao
        // salva (comum: descricao e sempre opcional) fica com o campo vazio e
        // trava o envio do formulario inteiro sem nenhum erro visivel
        campoTexto.required = false;
        campoTexto.value = descricao || "";
        document.getElementById(`${prefixo}-local-id`).value = "";
        document.getElementById(`${prefixo}-cidade-id`).value = cidadeId || "";
        document.getElementById(`${prefixo}-descricao`).value = descricao || "";
    }
}

async function salvarEdicaoSolicitacao(evento) {
    evento.preventDefault();
    const mensagemErro = document.getElementById("mensagem-erro-editar-solicitacao");
    mensagemErro.classList.add("d-none");

    const valorOuNulo = (id) => {
        const valor = document.getElementById(id).value;
        return valor === "" ? null : valor;
    };

    const corpo = {
        localOrigemId: valorOuNulo("sol-origem-local-id") ? Number(valorOuNulo("sol-origem-local-id")) : null,
        cidadeOrigemId: valorOuNulo("sol-origem-cidade-id") ? Number(valorOuNulo("sol-origem-cidade-id")) : null,
        descricaoOrigem: valorOuNulo("sol-origem-descricao"),
        localDestinoId: valorOuNulo("sol-destino-local-id") ? Number(valorOuNulo("sol-destino-local-id")) : null,
        cidadeDestinoId: valorOuNulo("sol-destino-cidade-id") ? Number(valorOuNulo("sol-destino-cidade-id")) : null,
        descricaoDestino: valorOuNulo("sol-destino-descricao"),
        dataHoraDesejada: seletorDataEdicaoSolicitacao.obterValor(),
        dataHoraRetornoDesejada: seletorRetornoEdicaoSolicitacao.obterValor(),
        qtdPassageiros: Number(document.getElementById("input-editar-sol-passageiros").value),
        telefoneContato: document.getElementById("input-editar-sol-telefone").value,
        finalidade: document.getElementById("input-editar-sol-finalidade").value,
        observacoes: document.getElementById("input-editar-sol-observacoes").value,
    };

    try {
        await Api.patch(`/api/solicitacoes/${solicitacaoSelecionada.id}`, corpo);
        modalDetalheSolicitacao.hide();
        Ui.mostrarToast("Solicitacao atualizada.");
        await renderizar();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

async function cancelarSolicitacaoCalendario() {
    const mensagemErro = document.getElementById("mensagem-erro-cancelar-solicitacao");
    mensagemErro.classList.add("d-none");

    const confirmado = await Ui.confirmarAcao("Cancelar esta solicitacao?", "Cancelar solicitacao");
    if (!confirmado) {
        return;
    }

    try {
        await Api.patch(`/api/solicitacoes/${solicitacaoSelecionada.id}/cancelar`);
        modalDetalheSolicitacao.hide();
        Ui.mostrarToast("Solicitacao cancelada.");
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
    const viagens = await Api.get(`/api/viagens/calendario?${parametros.toString()}`);
    return viagens.filter((viagem) => statusAtivos.has(viagem.status));
}

async function buscarSolicitacoesPendentes(inicio, fim) {
    const parametros = new URLSearchParams({
        inicio: formatarDataHoraLocal(inicio),
        fim: formatarDataHoraLocal(fim),
    });
    return Api.get(`/api/solicitacoes/calendario?${parametros.toString()}`);
}

// Junta viagens (ja confirmadas) e solicitacoes ainda sem viagem (pendente,
// reprovada, cancelada) num unico array pra grade do calendario, marcando o
// tipo de cada item e normalizando a data usada pra agrupar por dia.
async function buscarEventosCalendario(inicio, fim) {
    const [viagens, solicitacoes] = await Promise.all([
        buscarViagens(inicio, fim),
        buscarSolicitacoesPendentes(inicio, fim),
    ]);
    const viagensMarcadas = viagens.map((viagem) => ({ ...viagem, _tipo: "viagem", _dataReferencia: viagem.dataHoraSaida }));
    const solicitacoesMarcadas = solicitacoes.map((solicitacao) => ({ ...solicitacao, _tipo: "solicitacao", _dataReferencia: solicitacao.dataHoraDesejada }));
    return [...viagensMarcadas, ...solicitacoesMarcadas];
}

function formatarDataHoraLocal(data) {
    const pad = (numero) => String(numero).padStart(2, "0");
    return `${data.getFullYear()}-${pad(data.getMonth() + 1)}-${pad(data.getDate())}T${pad(data.getHours())}:${pad(data.getMinutes())}:00`;
}
