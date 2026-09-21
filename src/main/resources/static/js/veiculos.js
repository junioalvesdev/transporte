let modalVeiculo = null;
let paginaAtualVeiculos = 0;

document.addEventListener("DOMContentLoaded", async () => {
    modalVeiculo = new bootstrap.Modal(document.getElementById("modal-veiculo"));
    await Promise.all([carregarVeiculos(), carregarTiposVeiculo()]);
    document.getElementById("form-veiculo").addEventListener("submit", salvar);
    document.getElementById("botao-novo-veiculo").addEventListener("click", abrirModalNovo);
});

async function carregarVeiculos(pagina = 0) {
    paginaAtualVeiculos = pagina;
    // "do ultimo ao primeiro" ja vem pronto do backend (ORDER BY id DESC)
    const resultado = await Api.get(`/api/veiculos?pagina=${pagina}&tamanho=10`);
    const tabela = document.getElementById("tabela-veiculos");
    tabela.innerHTML = "";
    resultado.conteudo.map(criarLinha).forEach((linha) => tabela.appendChild(linha));
    Ui.montarPaginacao(document.getElementById("paginacao-veiculos"), resultado, carregarVeiculos);
}

function criarLinha(veiculo) {
    const linha = document.createElement("tr");
    linha.innerHTML = `
        <td>${veiculo.placa}</td>
        <td>${veiculo.modelo}</td>
        <td>${veiculo.capacidade}</td>
        <td><i class="bi ${iconeParaTipoVeiculo(veiculo.tipoVeiculo)} me-1 text-muted"></i>${veiculo.tipoVeiculo}</td>
        <td class="text-end">
            <button class="btn btn-sm btn-ovg-secundario" data-acao="editar">Editar</button>
            <button class="btn btn-sm btn-outline-danger" data-acao="inativar">Inativar</button>
        </td>
    `;
    linha.querySelector('[data-acao="editar"]').addEventListener("click", () => abrirModalEdicao(veiculo));
    linha.querySelector('[data-acao="inativar"]').addEventListener("click", () => inativar(veiculo.id));
    return linha;
}

async function carregarTiposVeiculo() {
    const tipos = await Api.get("/api/tipos-veiculo");
    const select = document.getElementById("select-tipo-veiculo");
    tipos.map((tipo) => new Option(tipo.nome, tipo.id)).forEach((opcao) => select.add(opcao));
}

function abrirModalNovo() {
    document.getElementById("form-veiculo").reset();
    document.getElementById("editando-id-veiculo").value = "";
    document.getElementById("input-placa").disabled = false;
    document.getElementById("titulo-modal-veiculo").textContent = "Novo veiculo";
    document.getElementById("botao-salvar-veiculo").textContent = "Salvar";
    document.getElementById("mensagem-erro-veiculo").classList.add("d-none");
    modalVeiculo.show();
}

function abrirModalEdicao(veiculo) {
    document.getElementById("form-veiculo").reset();
    document.getElementById("editando-id-veiculo").value = veiculo.id;
    document.getElementById("input-placa").value = veiculo.placa;
    // placa e a chave natural do veiculo: nao pode ser editada depois de cadastrada
    document.getElementById("input-placa").disabled = true;
    document.getElementById("input-modelo").value = veiculo.modelo;
    document.getElementById("input-capacidade").value = veiculo.capacidade;
    const selectTipo = document.getElementById("select-tipo-veiculo");
    const opcaoTipo = [...selectTipo.options].find((opcao) => opcao.text === veiculo.tipoVeiculo);
    if (opcaoTipo) {
        selectTipo.value = opcaoTipo.value;
    }
    document.getElementById("titulo-modal-veiculo").textContent = `Editar veiculo — ${veiculo.placa}`;
    document.getElementById("botao-salvar-veiculo").textContent = "Salvar alteracoes";
    document.getElementById("mensagem-erro-veiculo").classList.add("d-none");
    modalVeiculo.show();
}

async function salvar(evento) {
    evento.preventDefault();
    const mensagemErro = document.getElementById("mensagem-erro-veiculo");
    mensagemErro.classList.add("d-none");

    const idEditando = document.getElementById("editando-id-veiculo").value;
    const corpo = {
        placa: document.getElementById("input-placa").value,
        modelo: document.getElementById("input-modelo").value,
        capacidade: Number(document.getElementById("input-capacidade").value),
        tipoVeiculoId: Number(document.getElementById("select-tipo-veiculo").value),
    };

    try {
        if (idEditando) {
            await Api.put(`/api/veiculos/${idEditando}`, corpo);
            Ui.mostrarToast("Veiculo atualizado.");
        } else {
            await Api.post("/api/veiculos", corpo);
            Ui.mostrarToast("Veiculo cadastrado.");
        }
        modalVeiculo.hide();
        await carregarVeiculos();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

async function inativar(id) {
    const confirmado = await Ui.confirmarAcao("Inativar este veiculo?", "Inativar");
    if (!confirmado) {
        return;
    }
    try {
        await Api.delete(`/api/veiculos/${id}`);
        Ui.mostrarToast("Veiculo inativado.");
        await carregarVeiculos();
    } catch (erro) {
        Ui.mostrarToast(erro.message, "erro");
    }
}
