/**
 * Seletor de data + hora: calendario (flatpickr, inline) com um seletor de
 * hora/minuto do lado no estilo "roda" do Android — um cartao mostrando 7
 * itens, o do meio e o selecionavel, rolando pra cima/baixo. Tudo dentro do
 * mesmo popup, que abre ao clicar no campo, comeca posicionado na hora/minuto
 * atual e fecha sozinho assim que o minuto e escolhido.
 */
const ALTURA_ITEM_RODA = 36;
const ITENS_VISIVEIS_RODA = 7;
const PADDING_ITENS_RODA = Math.floor(ITENS_VISIVEIS_RODA / 2);
const VALORES_HORA = Array.from({ length: 24 }, (_, hora) => String(hora).padStart(2, "0"));
const VALORES_MINUTO = Array.from({ length: 60 }, (_, minuto) => String(minuto).padStart(2, "0"));

function montarSeletorDataHora(idContainer, rotulo, obrigatorio = true, placeholder = "Selecionar data e hora") {
    const container = document.getElementById(idContainer);
    const prefixo = idContainer;
    container.classList.add("seletor-datahora");

    container.innerHTML = `
        <label class="form-label">${rotulo}</label>
        <input type="text" class="form-control" id="${prefixo}-exibicao" placeholder="${placeholder}" readonly autocomplete="off" ${obrigatorio ? "required" : ""}>
        <div class="seletor-datahora-popup" id="${prefixo}-popup">
            <div id="${prefixo}-calendario"></div>
            <div class="roda-tempo-grupo">
                <div class="roda-tempo-coluna">
                    <div class="roda-tempo-marcador"></div>
                    <div class="roda-tempo" id="${prefixo}-roda-hora"></div>
                </div>
                <div class="roda-tempo-separador">:</div>
                <div class="roda-tempo-coluna">
                    <div class="roda-tempo-marcador"></div>
                    <div class="roda-tempo" id="${prefixo}-roda-minuto"></div>
                </div>
            </div>
        </div>
    `;

    const campoExibicao = document.getElementById(`${prefixo}-exibicao`);
    const popup = document.getElementById(`${prefixo}-popup`);

    const agora = new Date();
    let dataSelecionada = null;
    let horaSelecionada = String(agora.getHours()).padStart(2, "0");
    let minutoSelecionada = String(agora.getMinutes()).padStart(2, "0");

    function atualizarExibicao() {
        if (!dataSelecionada) {
            campoExibicao.value = "";
            return;
        }
        const [ano, mes, dia] = dataSelecionada.split("-");
        campoExibicao.value = `${dia}/${mes}/${ano} ${horaSelecionada}:${minutoSelecionada}`;
    }

    function fecharPopup() {
        popup.classList.remove("aberto");
    }

    const calendario = flatpickr(document.getElementById(`${prefixo}-calendario`), {
        locale: "pt",
        inline: true,
        dateFormat: "Y-m-d",
        onChange: (datas, dataStr) => {
            dataSelecionada = dataStr;
            atualizarExibicao();
        },
    });

    const rodaHora = criarRodaTempo(
        document.getElementById(`${prefixo}-roda-hora`),
        VALORES_HORA,
        horaSelecionada,
        (valor) => { horaSelecionada = valor; atualizarExibicao(); }
    );
    const rodaMinuto = criarRodaTempo(
        document.getElementById(`${prefixo}-roda-minuto`),
        VALORES_MINUTO,
        minutoSelecionada,
        (valor) => {
            minutoSelecionada = valor;
            atualizarExibicao();
            // fecha sozinho ao terminar de escolher o minuto
            setTimeout(fecharPopup, 250);
        }
    );

    campoExibicao.addEventListener("click", () => {
        document.querySelectorAll(".seletor-datahora-popup.aberto").forEach((outroPopup) => {
            if (outroPopup !== popup) {
                outroPopup.classList.remove("aberto");
            }
        });
        const vaiAbrir = !popup.classList.contains("aberto");
        popup.classList.toggle("aberto");
        if (vaiAbrir) {
            // garante que a roda esta na posicao certa: o navegador pode nao
            // aplicar scrollTo direito no primeiro instante em que o popup
            // passa a ficar visivel
            rodaHora.definirValor(horaSelecionada);
            rodaMinuto.definirValor(minutoSelecionada);
        }
    });

    document.addEventListener("click", (evento) => {
        if (!container.contains(evento.target)) {
            fecharPopup();
        }
    });

    return {
        obterValor() {
            return dataSelecionada ? `${dataSelecionada}T${horaSelecionada}:${minutoSelecionada}` : "";
        },
        definirValor(valorIso) {
            if (!valorIso) {
                this.limpar();
                return;
            }
            const [dataParte, horaParte] = valorIso.split("T");
            const [hora, minuto] = horaParte.split(":");
            dataSelecionada = dataParte;
            horaSelecionada = hora;
            minutoSelecionada = minuto;
            calendario.setDate(dataParte, false);
            rodaHora.definirValor(hora);
            rodaMinuto.definirValor(minuto);
            atualizarExibicao();
        },
        definirDataMinima(data) {
            calendario.set("minDate", data);
        },
        limpar() {
            dataSelecionada = null;
            calendario.clear();
            campoExibicao.value = "";
        },
    };
}

/**
 * Calendario de varios dias (pra "replicar viagem") com uma roda de
 * hora/minuto do lado, no mesmo estilo visual do seletor de data/hora unico
 * — sempre visivel (nao e um popup), entao nao precisa abrir/fechar.
 */
function montarSeletorDiasReplicados(idContainer) {
    const container = document.getElementById(idContainer);
    const prefixo = idContainer;
    container.classList.add("roda-tempo-grupo", "align-items-start");

    container.innerHTML = `
        <div id="${prefixo}-calendario"></div>
        <div class="roda-tempo-grupo">
            <div class="roda-tempo-coluna">
                <div class="roda-tempo-marcador"></div>
                <div class="roda-tempo" id="${prefixo}-roda-hora"></div>
            </div>
            <div class="roda-tempo-separador">:</div>
            <div class="roda-tempo-coluna">
                <div class="roda-tempo-marcador"></div>
                <div class="roda-tempo" id="${prefixo}-roda-minuto"></div>
            </div>
        </div>
    `;

    const agora = new Date();
    let horaSelecionada = String(agora.getHours()).padStart(2, "0");
    let minutoSelecionada = String(agora.getMinutes()).padStart(2, "0");

    const calendario = flatpickr(document.getElementById(`${prefixo}-calendario`), {
        locale: "pt",
        inline: true,
        mode: "multiple",
        dateFormat: "Y-m-d",
    });

    criarRodaTempo(document.getElementById(`${prefixo}-roda-hora`), VALORES_HORA, horaSelecionada, (valor) => { horaSelecionada = valor; });
    criarRodaTempo(document.getElementById(`${prefixo}-roda-minuto`), VALORES_MINUTO, minutoSelecionada, (valor) => { minutoSelecionada = valor; });

    return {
        definirDataMinima(data) {
            calendario.set("minDate", data);
        },
        obterDias() {
            return calendario.selectedDates.map((data) => flatpickr.formatDate(data, "Y-m-d"));
        },
        obterHorario() {
            return `${horaSelecionada}:${minutoSelecionada}`;
        },
        limpar() {
            calendario.clear();
        },
    };
}

/**
 * Uma coluna de "roda" (estilo Android): lista vertical com scroll-snap, N
 * itens visiveis, o do meio e o valor selecionado. Rolar ou clicar num item
 * troca o valor.
 */
function criarRodaTempo(elementoRoda, valores, valorInicial, aoMudar) {
    elementoRoda.innerHTML = "";

    const espacador = () => {
        const div = document.createElement("div");
        div.style.height = `${ALTURA_ITEM_RODA}px`;
        return div;
    };

    Array.from({ length: PADDING_ITENS_RODA }).forEach(() => elementoRoda.appendChild(espacador()));
    valores
        .map((valor) => {
            const item = document.createElement("div");
            item.className = "roda-tempo-item";
            item.textContent = valor;
            item.dataset.valor = valor;
            return item;
        })
        .forEach((item) => elementoRoda.appendChild(item));
    Array.from({ length: PADDING_ITENS_RODA }).forEach(() => elementoRoda.appendChild(espacador()));

    let valorAtual = valorInicial;

    function marcarSelecionado() {
        elementoRoda.querySelectorAll(".roda-tempo-item").forEach((item) => {
            item.classList.toggle("selecionado", item.dataset.valor === valorAtual);
        });
    }

    function rolarPara(valor, suave) {
        const indice = valores.indexOf(valor);
        if (indice === -1) {
            return;
        }
        elementoRoda.scrollTo({ top: indice * ALTURA_ITEM_RODA, behavior: suave ? "smooth" : "auto" });
    }

    let temporizadorScroll = null;
    elementoRoda.addEventListener("scroll", () => {
        clearTimeout(temporizadorScroll);
        temporizadorScroll = setTimeout(() => {
            const indice = Math.round(elementoRoda.scrollTop / ALTURA_ITEM_RODA);
            const valor = valores[Math.min(Math.max(indice, 0), valores.length - 1)];
            rolarPara(valor, true);
            if (valor !== valorAtual) {
                valorAtual = valor;
                marcarSelecionado();
                aoMudar(valorAtual);
            }
        }, 120);
    });

    elementoRoda.querySelectorAll(".roda-tempo-item").forEach((item) => {
        item.addEventListener("click", () => {
            valorAtual = item.dataset.valor;
            rolarPara(valorAtual, true);
            marcarSelecionado();
            aoMudar(valorAtual);
        });
    });

    rolarPara(valorInicial, false);
    marcarSelecionado();

    return {
        definirValor(valor) {
            valorAtual = valor;
            rolarPara(valor, false);
            marcarSelecionado();
        },
    };
}
