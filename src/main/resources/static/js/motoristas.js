let modalMotorista = null;

document.addEventListener("DOMContentLoaded", async () => {
    modalMotorista = new bootstrap.Modal(document.getElementById("modal-motorista"));
    await carregarMotoristas();
    document.getElementById("form-motorista").addEventListener("submit", salvar);
    document.getElementById("botao-novo-motorista").addEventListener("click", abrirModalNovo);
});

async function carregarMotoristas(pagina = 0) {
    const resultado = await Api.get(`/api/motoristas?pagina=${pagina}&tamanho=10`);
    const tabela = document.getElementById("tabela-motoristas");
    tabela.innerHTML = "";
    resultado.conteudo.map(criarLinha).forEach((linha) => tabela.appendChild(linha));
    Ui.montarPaginacao(document.getElementById("paginacao-motoristas"), resultado, carregarMotoristas);
}

function criarLinha(motorista) {
    const linha = document.createElement("tr");
    linha.innerHTML = `
        <td>${motorista.nome}</td>
        <td>${motorista.telefone ?? ""}</td>
        <td class="text-end">
            <button class="btn btn-sm btn-ovg-secundario" data-acao="editar">Editar</button>
            <button class="btn btn-sm btn-outline-danger" data-acao="inativar">Inativar</button>
        </td>
    `;
    linha.querySelector('[data-acao="editar"]').addEventListener("click", () => abrirModalEdicao(motorista));
    linha.querySelector('[data-acao="inativar"]').addEventListener("click", () => inativar(motorista.id));
    return linha;
}

function abrirModalNovo() {
    document.getElementById("form-motorista").reset();
    document.getElementById("editando-id-motorista").value = "";
    document.getElementById("titulo-modal-motorista").textContent = "Novo motorista";
    document.getElementById("botao-salvar-motorista").textContent = "Salvar";
    document.getElementById("mensagem-erro-motorista").classList.add("d-none");
    modalMotorista.show();
}

function abrirModalEdicao(motorista) {
    document.getElementById("form-motorista").reset();
    document.getElementById("editando-id-motorista").value = motorista.id;
    document.getElementById("input-nome").value = motorista.nome;
    document.getElementById("input-telefone").value = motorista.telefone ?? "";
    document.getElementById("titulo-modal-motorista").textContent = `Editar motorista — ${motorista.nome}`;
    document.getElementById("botao-salvar-motorista").textContent = "Salvar alteracoes";
    document.getElementById("mensagem-erro-motorista").classList.add("d-none");
    modalMotorista.show();
}

async function salvar(evento) {
    evento.preventDefault();
    const mensagemErro = document.getElementById("mensagem-erro-motorista");
    mensagemErro.classList.add("d-none");

    const idEditando = document.getElementById("editando-id-motorista").value;
    const corpo = {
        nome: document.getElementById("input-nome").value,
        telefone: document.getElementById("input-telefone").value,
    };

    try {
        if (idEditando) {
            await Api.put(`/api/motoristas/${idEditando}`, corpo);
            Ui.mostrarToast("Motorista atualizado.");
        } else {
            await Api.post("/api/motoristas", corpo);
            Ui.mostrarToast("Motorista cadastrado.");
        }
        modalMotorista.hide();
        await carregarMotoristas();
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}

async function inativar(id) {
    const confirmado = await Ui.confirmarAcao("Inativar este motorista?", "Inativar");
    if (!confirmado) {
        return;
    }
    try {
        await Api.delete(`/api/motoristas/${id}`);
        Ui.mostrarToast("Motorista inativado.");
        await carregarMotoristas();
    } catch (erro) {
        Ui.mostrarToast(erro.message, "erro");
    }
}
