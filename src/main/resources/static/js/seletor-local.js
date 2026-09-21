/**
 * Componente de "origem"/"destino": um select nativo com as unidades
 * administrativas cadastradas (igual ao sistema legado) mais uma opcao
 * "Outro Endereco" no fim — ao
 * escolher "Outro Endereco", o select vira um campo de texto pra buscar
 * cidade (com opcao de descrever o endereco). Preenche campos escondidos que
 * o formulario le no submit:
 *   #<prefixo>-local-id    -> id da unidade, OU vazio
 *   #<prefixo>-cidade-id   -> id da cidade, OU vazio
 *   #<prefixo>-descricao   -> nao usado mais, mantido vazio por compatibilidade
 * Exatamente um dos dois (local OU cidade) fica preenchido por vez.
 */
const VALOR_OUTRO_ENDERECO = "__outro__";

async function montarSeletorLocal(prefixo, rotulo, elementoContainer) {
    const unidades = await Api.get("/api/locais-administrativos");
    if (!unidades) {
        // sessao expirada: Api.get ja redirecionou pro login, so nao ha nada
        // mais a montar aqui (evita "Cannot read properties of null" na tela)
        return;
    }
    window.transportesUnidadesPorPrefixo = window.transportesUnidadesPorPrefixo || {};
    window.transportesUnidadesPorPrefixo[prefixo] = unidades;

    elementoContainer.innerHTML = `
        <label class="form-label">${rotulo}</label>
        <select class="form-select" data-campo="select-unidade" required></select>
        <div data-bloco="outro" class="d-none">
            <button type="button" class="btn btn-link btn-sm p-0 mb-1" data-campo="voltar">&larr; Escolher unidade cadastrada</button>
            <input class="form-control" data-campo="texto-cidade" list="lista-${prefixo}" autocomplete="off" placeholder="Buscar cidade">
            <datalist id="lista-${prefixo}"></datalist>
        </div>
        <input type="hidden" id="${prefixo}-local-id">
        <input type="hidden" id="${prefixo}-cidade-id">
        <input type="hidden" id="${prefixo}-descricao">
    `;

    const selectUnidade = elementoContainer.querySelector('[data-campo="select-unidade"]');
    unidades.map((unidade) => new Option(unidade.nome, unidade.id)).forEach((opcao) => selectUnidade.add(opcao));
    selectUnidade.add(new Option("Outro Endereco", VALOR_OUTRO_ENDERECO));

    if (unidades.length > 0) {
        document.getElementById(`${prefixo}-local-id`).value = unidades[0].id;
    }

    const blocoOutro = elementoContainer.querySelector('[data-bloco="outro"]');
    const botaoVoltar = elementoContainer.querySelector('[data-campo="voltar"]');
    const campoTexto = elementoContainer.querySelector('[data-campo="texto-cidade"]');
    const datalist = elementoContainer.querySelector(`#lista-${prefixo}`);

    function entrarModoOutro() {
        selectUnidade.classList.add("d-none");
        blocoOutro.classList.remove("d-none");
        campoTexto.required = true;
        document.getElementById(`${prefixo}-local-id`).value = "";
        campoTexto.focus();
    }

    function voltarModoUnidade() {
        selectUnidade.classList.remove("d-none");
        blocoOutro.classList.add("d-none");
        campoTexto.required = false;
        campoTexto.value = "";
        datalist.innerHTML = "";
        document.getElementById(`${prefixo}-cidade-id`).value = "";
        document.getElementById(`${prefixo}-descricao`).value = "";
        selectUnidade.value = unidades.length > 0 ? String(unidades[0].id) : VALOR_OUTRO_ENDERECO;
        document.getElementById(`${prefixo}-local-id`).value = unidades.length > 0 ? unidades[0].id : "";
    }

    selectUnidade.addEventListener("change", () => {
        if (selectUnidade.value === VALOR_OUTRO_ENDERECO) {
            entrarModoOutro();
        } else {
            document.getElementById(`${prefixo}-local-id`).value = selectUnidade.value;
            document.getElementById(`${prefixo}-cidade-id`).value = "";
        }
    });

    botaoVoltar.addEventListener("click", voltarModoUnidade);

    let cidadesEncontradas = [];
    let temporizador = null;

    campoTexto.addEventListener("input", () => {
        document.getElementById(`${prefixo}-cidade-id`).value = "";
        clearTimeout(temporizador);
        const termo = campoTexto.value.trim();
        if (termo.length < 2) {
            return;
        }
        temporizador = setTimeout(async () => {
            cidadesEncontradas = await Api.get(`/api/cidades?nome=${encodeURIComponent(termo)}`);
            datalist.innerHTML = "";
            cidadesEncontradas
                .map((cidade) => { const opcao = document.createElement("option"); opcao.value = `${cidade.nome}/${cidade.uf}`; return opcao; })
                .forEach((opcao) => datalist.appendChild(opcao));
        }, 250);
    });
    campoTexto.addEventListener("change", () => {
        const escolhida = cidadesEncontradas.find((cidade) => `${cidade.nome}/${cidade.uf}` === campoTexto.value);
        document.getElementById(`${prefixo}-cidade-id`).value = escolhida ? escolhida.id : "";
    });
}

/**
 * Resolve a cidade por tras da escolha atual do seletor (unidade ou cidade
 * direta) — usado pela busca de viagens disponiveis, que so entende cidade.
 */
function obterCidadeEfetiva(prefixo) {
    const localId = document.getElementById(`${prefixo}-local-id`).value;
    if (localId) {
        const unidades = (window.transportesUnidadesPorPrefixo || {})[prefixo] || [];
        const unidade = unidades.find((u) => String(u.id) === localId);
        return unidade ? unidade.cidadeId : null;
    }
    const cidadeId = document.getElementById(`${prefixo}-cidade-id`).value;
    return cidadeId ? Number(cidadeId) : null;
}
