/**
 * Modal "Nova solicitacao", acessivel de qualquer aba da Central de
 * Transportes sem precisar navegar pra outra pagina.
 */
let seletorDataHoraSaida = null;
let seletorDataHoraRetorno = null;
let seletorDiasReplicados = null;

document.addEventListener("DOMContentLoaded", () => {
    montarSeletorLocal("origem", "Origem", document.getElementById("seletor-origem"));
    montarSeletorLocal("destino", "Destino", document.getElementById("seletor-destino"));

    // impede escolher uma data/hora que ja passou (o backend exige @Future).
    const agora = new Date();
    seletorDataHoraSaida = montarSeletorDataHora(
        "campo-data-hora",
        "Data e horario desejados",
        true,
        "Selecione data e hora - 2h de antecedencia e considerado urgente"
    );
    seletorDataHoraRetorno = montarSeletorDataHora("campo-data-hora-retorno", "Retorno estimado", true);
    seletorDataHoraSaida.definirDataMinima(agora);
    seletorDataHoraRetorno.definirDataMinima(agora);

    document.getElementById("replicar-outros-dias").addEventListener("change", (evento) => {
        document.getElementById("campo-dias-replicados").classList.toggle("d-none", !evento.target.checked);
        // criado so na primeira vez que o checkbox e marcado: o flatpickr e a
        // roda de hora precisam de dimensoes reais, e a secao comeca escondida
        if (evento.target.checked && !seletorDiasReplicados) {
            seletorDiasReplicados = montarSeletorDiasReplicados("calendario-dias-replicados");
            seletorDiasReplicados.definirDataMinima(agora);
        }
        if (!evento.target.checked && seletorDiasReplicados) {
            seletorDiasReplicados.limpar();
            reabrirDiasReplicados();
        }
    });

    document.getElementById("botao-confirmar-dias-replicados").addEventListener("click", confirmarDiasReplicados);

    aplicarMascaraTelefone(document.getElementById("telefone-contato"));

    document.getElementById("form-solicitacao").addEventListener("submit", enviarSolicitacao);
});

// formata como "(XX) XXXXX-XXXX" (celular) ou "(XX) XXXX-XXXX" (fixo) conforme digita
function aplicarMascaraTelefone(campo) {
    campo.addEventListener("input", () => {
        const digitos = campo.value.replace(/\D/g, "").slice(0, 11);
        let formatado = digitos;
        if (digitos.length > 2) {
            formatado = `(${digitos.slice(0, 2)}) ${digitos.slice(2)}`;
        }
        if (digitos.length > 7) {
            const tamanhoNumero = digitos.length > 10 ? 5 : 4;
            formatado = `(${digitos.slice(0, 2)}) ${digitos.slice(2, 2 + tamanhoNumero)}-${digitos.slice(2 + tamanhoNumero)}`;
        }
        campo.value = formatado;
    });
}

function confirmarDiasReplicados() {
    const dias = seletorDiasReplicados.obterDias();
    if (dias.length === 0) {
        Ui.mostrarToast("Escolha pelo menos um dia para replicar.", "erro");
        return;
    }

    const horario = seletorDiasReplicados.obterHorario();
    const datasFormatadas = dias
        .map((dia) => { const [ano, mes, diaNumero] = dia.split("-"); return `${diaNumero}/${mes}/${ano}`; })
        .join(", ");

    document.getElementById("calendario-dias-replicados").classList.add("d-none");
    document.getElementById("botao-confirmar-dias-replicados").classList.add("d-none");

    const resumo = document.getElementById("resumo-dias-replicados");
    resumo.innerHTML = `
        <strong>${dias.length} dia(s) replicado(s)</strong> as ${horario}: ${datasFormatadas}
        <button type="button" class="btn btn-link btn-sm p-0 ms-2" id="botao-editar-dias-replicados">Editar</button>
    `;
    resumo.classList.remove("d-none");
    document.getElementById("botao-editar-dias-replicados").addEventListener("click", reabrirDiasReplicados);
}

function reabrirDiasReplicados() {
    document.getElementById("calendario-dias-replicados").classList.remove("d-none");
    document.getElementById("botao-confirmar-dias-replicados").classList.remove("d-none");
    ocultarResumoDiasReplicados();
}

function ocultarResumoDiasReplicados() {
    const resumo = document.getElementById("resumo-dias-replicados");
    resumo.classList.add("d-none");
    resumo.innerHTML = "";
}

// troca so a parte da data (yyyy-mm-dd) de um "yyyy-mm-ddTHH:mm", mantendo o horario
function comNovaData(valorIso, novaData) {
    const [, horaParte] = valorIso.split("T");
    return `${novaData}T${horaParte}`;
}

async function enviarSolicitacao(evento) {
    evento.preventDefault();
    const mensagemErro = document.getElementById("mensagem-erro-solicitacao");
    mensagemErro.classList.add("d-none");

    const valorOuNulo = (id) => {
        const valor = document.getElementById(id).value;
        return valor === "" ? null : valor;
    };

    const dadosBase = {
        localOrigemId: valorOuNulo("origem-local-id") ? Number(valorOuNulo("origem-local-id")) : null,
        cidadeOrigemId: valorOuNulo("origem-cidade-id") ? Number(valorOuNulo("origem-cidade-id")) : null,
        descricaoOrigem: valorOuNulo("origem-descricao"),
        localDestinoId: valorOuNulo("destino-local-id") ? Number(valorOuNulo("destino-local-id")) : null,
        cidadeDestinoId: valorOuNulo("destino-cidade-id") ? Number(valorOuNulo("destino-cidade-id")) : null,
        descricaoDestino: valorOuNulo("destino-descricao"),
        qtdPassageiros: Number(document.getElementById("qtd-passageiros-solicitacao").value),
        telefoneContato: document.getElementById("telefone-contato").value,
        finalidade: document.getElementById("finalidade").value,
        observacoes: document.getElementById("observacoes").value,
    };

    const dataHoraDesejada = seletorDataHoraSaida.obterValor();
    const dataHoraRetornoDesejada = seletorDataHoraRetorno.obterValor();

    const replicar = document.getElementById("replicar-outros-dias").checked;
    const diasReplicados = replicar && seletorDiasReplicados ? seletorDiasReplicados.obterDias() : [];
    const horarioReplicado = replicar && seletorDiasReplicados ? seletorDiasReplicados.obterHorario() : null;

    try {
        await Api.post("/api/solicitacoes", { ...dadosBase, dataHoraDesejada, dataHoraRetornoDesejada });

        // "replicar para outros dias": mesma solicitacao, com o horario de
        // saida escolhido na roda ao lado do calendario de replicacao, e o
        // retorno mantendo o mesmo horario-do-dia da solicitacao principal.
        for (const dia of diasReplicados) {
            await Api.post("/api/solicitacoes", {
                ...dadosBase,
                dataHoraDesejada: `${dia}T${horarioReplicado}`,
                dataHoraRetornoDesejada: dataHoraRetornoDesejada ? comNovaData(dataHoraRetornoDesejada, dia) : null,
            });
        }

        bootstrap.Modal.getInstance(document.getElementById("modal-nova-solicitacao")).hide();
        document.getElementById("form-solicitacao").reset();
        seletorDataHoraSaida.limpar();
        seletorDataHoraRetorno.limpar();
        if (seletorDiasReplicados) {
            seletorDiasReplicados.limpar();
            reabrirDiasReplicados();
        }
        document.getElementById("campo-dias-replicados").classList.add("d-none");
        Ui.mostrarToast(diasReplicados.length > 0
            ? `Solicitacao enviada e replicada para mais ${diasReplicados.length} dia(s)!`
            : "Solicitacao enviada com sucesso!");

        // se a aba "Minhas solicitacoes" ja foi aberta antes, atualiza ela
        // tambem, pra solicitacao nova aparecer sem precisar recarregar a pagina
        if (typeof minhasSolicitacoesCarregadas !== "undefined" && minhasSolicitacoesCarregadas) {
            await carregarMinhasSolicitacoes();
        }
    } catch (erro) {
        mensagemErro.textContent = erro.message;
        mensagemErro.classList.remove("d-none");
    }
}
