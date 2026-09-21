/**
 * "Lista de Reservas" — reimplementacao em Java da tela equivalente do
 * sistema legado: todas as solicitacoes juntas (qualquer status), agrupadas
 * por mes, numa tabela. Diferente de "Solicitacoes pendentes" (que so mostra
 * o que precisa de acao), aqui e uma visao administrativa geral, com filtros
 * e auto-refresh.
 */
const rotuloPorStatusSolicitacao = {
    PENDENTE: "Aguardando",
    APROVADA: "Disponivel",
    ATENDIDA: "Atendida",
    REPROVADA: "Reprovada",
    CANCELADA: "Cancelada",
};

const classePorStatusSolicitacaoReserva = {
    PENDENTE: "status-badge-pendente",
    APROVADA: "status-badge-aprovada",
    ATENDIDA: "status-badge-aprovada",
    REPROVADA: "status-badge-reprovada",
    CANCELADA: "status-badge-cancelada",
};

const TODOS_STATUS_SOLICITACAO = Object.keys(rotuloPorStatusSolicitacao);

// status da VIAGEM (nao da solicitacao) — mesmos valores usados no calendario,
// pra deixar escolher com que status a viagem ja nasce ao aprovar
const TODOS_STATUS_VIAGEM = ["PLANEJADA", "ABERTA_PARA_APROVEITAMENTO", "LOTADA", "EM_ANDAMENTO", "CONCLUIDA", "CANCELADA"];
const MESES_RESERVAS = ["Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"];
const INTERVALO_AUTO_REFRESH_RESERVAS_MS = 20000;

// ordem igual ao sistema legado: urgente primeiro, depois aguardando, depois
// disponivel/atendida, e por ultimo reprovada/cancelada (dentro de cada
// grupo, a mais antiga aparece primeiro)
function prioridadeReserva(reserva) {
    if (reserva.urgente) return 0;
    if (reserva.status === "PENDENTE") return 1;
    if (reserva.status === "APROVADA" || reserva.status === "ATENDIDA") return 2;
    if (reserva.status === "REPROVADA") return 3;
    return 4;
}

let todasReservas = [];
let filtroStatusAtivos = new Set(TODOS_STATUS_SOLICITACAO);
let filtroDataDe = "";
let filtroDataAte = "";
let reservaSelecionada = null;
let veiculosCarregados = [];
let motoristasCarregados = [];
let seletorSaidaAprovar = null;
let seletorChegadaAprovar = null;
let modalDetalheReserva = null;

document.addEventListener("DOMContentLoaded", async () => {
    modalDetalheReserva = new bootstrap.Modal(document.getElementById("modal-detalhe-reserva"));
    seletorSaidaAprovar = montarSeletorDataHora("campo-input-saida", "Saida", true);
    seletorChegadaAprovar = montarSeletorDataHora("campo-input-chegada", "Retorno estimado", true);

    montarFiltroStatus();

    document.getElementById("form-aprovar").addEventListener("submit", confirmarAprovacao);
    document.getElementById("botao-reprovar").addEventListener("click", confirmarReprovacao);
    document.getElementById("select-tipo-veiculo-aprovar").addEventListener("change", filtrarVeiculosPorTipo);
    document.getElementById("botao-atualizar").addEventListener("click", async () => {
        await carregarReservas();
        Ui.mostrarToast("Lista atualizada.");
    });
    document.getElementById("botao-aplicar-filtros").addEventListener("click", aplicarFiltros);
    document.getElementById("botao-limpar-filtros").addEventListener("click", limparFiltros);

    await Promise.all([carregarVeiculos(), carregarMotoristas(), carregarTiposVeiculo(), carregarReservas()]);

    setInterval(() => {
        if (document.querySelector(".modal.show")) {
            return;
        }
        carregarReservas();
    }, INTERVALO_AUTO_REFRESH_RESERVAS_MS);
});

function montarFiltroStatus() {
    const alvo = document.getElementById("lista-filtro-status-reservas");
    alvo.innerHTML = "";
    TODOS_STATUS_SOLICITACAO.forEach((status) => {
        const item = document.createElement("div");
        item.className = "form-check";
        item.innerHTML = `
            <input class="form-check-input" type="checkbox" checked id="filtro-status-${status}" data-status="${status}">
            <label class="form-check-label" for="filtro-status-${status}">${rotuloPorStatusSolicitacao[status]}</label>
        `;
        alvo.appendChild(item);
    });
}

async function carregarReservas() {
    const resultado = await Api.get("/api/solicitacoes/todas?pagina=0&tamanho=1000");
    todasReservas = resultado.conteudo;
    renderizarReservas();
}

function aplicarFiltros() {
    filtroStatusAtivos = new Set(
        [...document.querySelectorAll("#lista-filtro-status-reservas input:checked")].map((input) => input.dataset.status)
    );
    filtroDataDe = document.getElementById("filtro-data-de").value;
    filtroDataAte = document.getElementById("filtro-data-ate").value;
    renderizarReservas();
}

function limparFiltros() {
    filtroStatusAtivos = new Set(TODOS_STATUS_SOLICITACAO);
    filtroDataDe = "";
    filtroDataAte = "";
    document.querySelectorAll("#lista-filtro-status-reservas input").forEach((input) => { input.checked = true; });
    document.getElementById("filtro-data-de").value = "";
    document.getElementById("filtro-data-ate").value = "";
    renderizarReservas();
}

function contarFiltrosAtivos() {
    let contagem = TODOS_STATUS_SOLICITACAO.length - filtroStatusAtivos.size;
    if (filtroDataDe) contagem += 1;
    if (filtroDataAte) contagem += 1;
    return contagem;
}

function renderizarReservas() {
    const filtros = contarFiltrosAtivos();
    document.getElementById("badge-filtros").textContent = `Filtros: ${filtros}`;
    document.getElementById("legenda-resultado").textContent = filtros === 0
        ? "Mostrando todas as reservas"
        : "Mostrando reservas filtradas";

    const filtradas = todasReservas.filter((reserva) => {
        if (!filtroStatusAtivos.has(reserva.status)) {
            return false;
        }
        const dataReserva = reserva.dataHoraDesejada.substring(0, 10);
        if (filtroDataDe && dataReserva < filtroDataDe) {
            return false;
        }
        if (filtroDataAte && dataReserva > filtroDataAte) {
            return false;
        }
        return true;
    });

    document.getElementById("mensagem-vazio").classList.toggle("d-none", filtradas.length > 0);

    const grupos = agruparPorMes(filtradas);
    const alvo = document.getElementById("grupos-meses");
    alvo.innerHTML = "";
    grupos.forEach((grupo) => alvo.appendChild(criarGrupoMes(grupo)));
}

function agruparPorMes(reservas) {
    const porChave = new Map();
    reservas.forEach((reserva) => {
        const data = new Date(reserva.dataHoraDesejada);
        const chave = `${data.getFullYear()}-${String(data.getMonth()).padStart(2, "0")}`;
        if (!porChave.has(chave)) {
            porChave.set(chave, { ano: data.getFullYear(), mes: data.getMonth(), reservas: [] });
        }
        porChave.get(chave).reservas.push(reserva);
    });

    return [...porChave.values()]
        .sort((a, b) => (b.ano - a.ano) || (b.mes - a.mes))
        .map((grupo) => {
            grupo.reservas.sort((a, b) => (prioridadeReserva(a) - prioridadeReserva(b))
                || (new Date(a.dataHoraDesejada) - new Date(b.dataHoraDesejada)));
            return grupo;
        });
}

function criarGrupoMes(grupo) {
    const idColapso = `mes-${grupo.ano}-${grupo.mes}`;
    const cartao = document.createElement("div");
    cartao.className = "cartao reservas-grupo-mes mb-3";
    cartao.innerHTML = `
        <button class="reservas-grupo-cabecalho" type="button" data-bs-toggle="collapse" data-bs-target="#${idColapso}">
            <span><i class="bi bi-calendar3 me-2"></i>${MESES_RESERVAS[grupo.mes]} ${grupo.ano}
                <span class="badge rounded-pill text-bg-secondary ms-2">${grupo.reservas.length} reserva(s)</span>
            </span>
            <i class="bi bi-chevron-down"></i>
        </button>
        <div class="collapse show" id="${idColapso}">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0 reservas-tabela">
                    <thead>
                        <tr>
                            <th>Requerente</th>
                            <th>Titulo</th>
                            <th>Origem &rarr; Destino</th>
                            <th>Data/Hora Partida</th>
                            <th>Data/Hora Retorno</th>
                            <th>Status</th>
                            <th>Usuarios</th>
                            <th>Veiculo</th>
                            <th>Descricao</th>
                            <th>Acoes</th>
                        </tr>
                    </thead>
                    <tbody></tbody>
                </table>
            </div>
        </div>
    `;

    const corpo = cartao.querySelector("tbody");
    grupo.reservas.map(criarLinhaReserva).forEach((linha) => corpo.appendChild(linha));
    return cartao;
}

function criarLinhaReserva(reserva) {
    const partida = new Date(reserva.dataHoraDesejada);
    const retorno = reserva.dataHoraRetornoDesejada ? new Date(reserva.dataHoraRetornoDesejada) : null;
    const descricao = reserva.observacoes || reserva.finalidade || "";

    const linha = document.createElement("tr");
    linha.innerHTML = `
        <td>
            <div class="fw-semibold"><i class="bi bi-person-fill me-1"></i>${reserva.solicitanteNome}</div>
            <div class="text-muted small"><i class="bi bi-telephone-fill me-1"></i>${reserva.telefoneContato || "-"}</div>
        </td>
        <td class="fw-semibold">${reserva.finalidade || "-"}</td>
        <td>
            <div><i class="bi bi-geo-alt-fill text-success me-1"></i>${reserva.origemExibicao}</div>
            <div><i class="bi bi-geo-alt-fill text-danger me-1"></i>${reserva.destinoExibicao}</div>
        </td>
        <td>${partida.toLocaleString("pt-BR")}</td>
        <td>${retorno ? retorno.toLocaleString("pt-BR") : "-"}</td>
        <td><span class="badge status-badge ${classePorStatusSolicitacaoReserva[reserva.status]}">${rotuloPorStatusSolicitacao[reserva.status]}</span></td>
        <td><span class="badge rounded-pill text-bg-info">${reserva.qtdPassageiros}</span></td>
        <td>${reserva.veiculoPlaca || "N/A"}</td>
        <td class="reservas-descricao"><button type="button" class="btn btn-link p-0 text-truncate d-block" style="max-width: 160px" data-acao="detalhe">${descricao || "-"}</button></td>
        <td><button type="button" class="btn btn-outline-secondary btn-sm" data-acao="detalhe"><i class="bi bi-eye-fill"></i></button></td>
    `;

    linha.querySelectorAll('[data-acao="detalhe"]').forEach((elemento) => {
        elemento.addEventListener("click", () => abrirDetalheReserva(reserva));
    });
    return linha;
}

function abrirDetalheReserva(reserva) {
    reservaSelecionada = reserva;
    document.getElementById("detalhe-reserva-requerente").textContent = reserva.solicitanteNome;
    document.getElementById("detalhe-reserva-telefone").textContent = reserva.telefoneContato || "-";
    document.getElementById("detalhe-reserva-titulo").textContent = reserva.finalidade || "-";
    document.getElementById("detalhe-reserva-origem").textContent = reserva.origemExibicao;
    document.getElementById("detalhe-reserva-destino").textContent = reserva.destinoExibicao;
    document.getElementById("detalhe-reserva-partida").textContent = new Date(reserva.dataHoraDesejada).toLocaleString("pt-BR");
    document.getElementById("detalhe-reserva-retorno").textContent = reserva.dataHoraRetornoDesejada
        ? new Date(reserva.dataHoraRetornoDesejada).toLocaleString("pt-BR")
        : "-";
    document.getElementById("detalhe-reserva-usuarios").textContent = reserva.qtdPassageiros;
    document.getElementById("detalhe-reserva-veiculo").textContent = reserva.veiculoPlaca || "N/A";
    document.getElementById("detalhe-reserva-status").innerHTML = `<span class="badge status-badge ${classePorStatusSolicitacaoReserva[reserva.status]}">${rotuloPorStatusSolicitacao[reserva.status]}</span>`;

    const temMotivo = Boolean(reserva.motivoRecusa);
    document.getElementById("detalhe-reserva-motivo-rotulo").classList.toggle("d-none", !temMotivo);
    document.getElementById("detalhe-reserva-motivo").classList.toggle("d-none", !temMotivo);
    document.getElementById("detalhe-reserva-motivo").textContent = reserva.motivoRecusa || "";

    const temObservacoes = Boolean(reserva.observacoes);
    document.getElementById("detalhe-reserva-observacoes-rotulo").classList.toggle("d-none", !temObservacoes);
    document.getElementById("detalhe-reserva-observacoes").classList.toggle("d-none", !temObservacoes);
    document.getElementById("detalhe-reserva-observacoes").textContent = reserva.observacoes || "";

    // enquanto PENDENTE, os campos de aprovar (veiculo/motorista/horarios) e o
    // motivo de reprovar ja aparecem aqui mesmo, sem abrir outro modal
    const ehPendente = reserva.status === "PENDENTE";
    document.getElementById("form-aprovar").classList.toggle("d-none", !ehPendente);
    document.getElementById("mensagem-erro-aprovar").classList.add("d-none");
    document.getElementById("mensagem-erro-reprovar").classList.add("d-none");
    document.getElementById("input-motivo-recusa").value = "";

    if (ehPendente) {
        seletorSaidaAprovar.definirValor(reserva.dataHoraDesejada.substring(0, 16));
        seletorChegadaAprovar.definirValor(reserva.dataHoraRetornoDesejada
            ? reserva.dataHoraRetornoDesejada.substring(0, 16)
            : "");
        document.getElementById("select-tipo-veiculo-aprovar").value = "";
        montarOpcoesVeiculo(veiculosCarregados);

        // viagem nova nasce ABERTA_PARA_APROVEITAMENTO por padrao; a pessoa
        // pode trocar aqui mesmo antes de aprovar
        const selectStatus = document.getElementById("select-status-aprovar");
        selectStatus.innerHTML = "";
        TODOS_STATUS_VIAGEM
            .map((status) => new Option(status, status, false, status === "ABERTA_PARA_APROVEITAMENTO"))
            .forEach((opcao) => selectStatus.add(opcao));
    }

    modalDetalheReserva.show();
}

async function confirmarAprovacao(evento) {
    evento.preventDefault();
    const mensagemErro = document.getElementById("mensagem-erro-aprovar");
    mensagemErro.classList.add("d-none");

    try {
        const viagem = await Api.patch(`/api/solicitacoes/${reservaSelecionada.id}/aprovar`, {
            novaViagem: {
                veiculoId: Number(document.getElementById("select-veiculo").value),
                motoristaId: Number(document.getElementById("select-motorista").value),
                dataHoraSaida: seletorSaidaAprovar.obterValor(),
                dataHoraChegadaEstimada: seletorChegadaAprovar.obterValor(),
                pontos: [
                    { localId: reservaSelecionada.localOrigemId, cidadeId: reservaSelecionada.cidadeOrigemId, ordem: 1, tipoPonto: "ORIGEM" },
                    { localId: reservaSelecionada.localDestinoId, cidadeId: reservaSelecionada.cidadeDestinoId, ordem: 2, tipoPonto: "DESTINO" },
                ],
            },
            cidadeEmbarqueId: reservaSelecionada.cidadeOrigemId,
            cidadeDesembarqueId: reservaSelecionada.cidadeDestinoId,
        });

        // a aprovacao ja aconteceu (solicitacao virou APROVADA e a viagem foi
        // criada) — se so a troca de status falhar (ex: transicao invalida),
        // isso NAO pode aparecer como se a aprovacao inteira tivesse falhado
        let avisoStatus = "";
        const novoStatus = document.getElementById("select-status-aprovar").value;
        if (novoStatus && novoStatus !== viagem.status) {
            try {
                await Api.patch(`/api/viagens/${viagem.id}/status?novoStatus=${novoStatus}`);
            } catch (erroStatus) {
                avisoStatus = ` A viagem ficou como ${viagem.status} (nao foi possivel mudar para ${novoStatus}: ${erroStatus.message}).`;
            }
        }

        modalDetalheReserva.hide();
        Ui.mostrarToast("Solicitacao aprovada e viagem criada." + avisoStatus);
        await carregarReservas();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

async function confirmarReprovacao() {
    const mensagemErro = document.getElementById("mensagem-erro-reprovar");
    mensagemErro.classList.add("d-none");
    const motivo = document.getElementById("input-motivo-recusa").value.trim();
    if (!motivo) {
        mensagemErro.textContent = "Informe o motivo da recusa.";
        mensagemErro.classList.remove("d-none");
        return;
    }

    try {
        await Api.patch(`/api/solicitacoes/${reservaSelecionada.id}/reprovar`, {
            motivoRecusa: motivo,
        });
        modalDetalheReserva.hide();
        Ui.mostrarToast("Solicitacao reprovada.");
        await carregarReservas();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

async function carregarVeiculos() {
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
    const resultado = await Api.get("/api/motoristas?pagina=0&tamanho=1000");
    const select = document.getElementById("select-motorista");
    resultado.conteudo
        .map((motorista) => new Option(motorista.nome, motorista.id))
        .forEach((opcao) => select.add(opcao));
}
