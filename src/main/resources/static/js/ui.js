/**
 * Componentes de interface reutilizaveis (toast de mensagem, modal de
 * confirmacao) — substituem alert()/confirm() nativos do navegador, que
 * destoam do resto do sistema e nao podem ser estilizados. Cada pagina que
 * usa este arquivo so precisa incluir <div id="toast-container"> no HTML
 * (ver estrutura em index.html) — o modal de confirmacao e criado sob
 * demanda, sem precisar de marcacao propria em cada pagina.
 */
const Ui = (() => {
    function mostrarToast(mensagem, tipo = "sucesso") {
        const container = document.getElementById("toast-container");
        if (!container) {
            // pagina sem o container: nao quebra, so avisa no console durante o desenvolvimento
            console.warn("toast-container ausente; mensagem:", mensagem);
            return;
        }

        const corPorTipo = { sucesso: "text-bg-success", erro: "text-bg-danger", aviso: "text-bg-warning" };
        const toast = document.createElement("div");
        toast.className = `toast align-items-center ${corPorTipo[tipo] || corPorTipo.sucesso} border-0`;
        toast.setAttribute("role", "alert");
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">${mensagem}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        container.appendChild(toast);

        const instancia = new bootstrap.Toast(toast, { delay: 4000 });
        toast.addEventListener("hidden.bs.toast", () => toast.remove());
        instancia.show();
    }

    function confirmarAcao(mensagem, tituloBotaoConfirmar = "Confirmar") {
        return new Promise((resolve) => {
            const elemento = document.createElement("div");
            elemento.className = "modal fade";
            elemento.innerHTML = `
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-body pt-4">${mensagem}</div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-ovg-secundario" data-acao="cancelar">Cancelar</button>
                            <button type="button" class="btn btn-ovg-principal" data-acao="confirmar">${tituloBotaoConfirmar}</button>
                        </div>
                    </div>
                </div>
            `;
            document.body.appendChild(elemento);
            const modal = new bootstrap.Modal(elemento);

            let confirmado = false;
            elemento.querySelector('[data-acao="confirmar"]').addEventListener("click", () => {
                confirmado = true;
                modal.hide();
            });
            elemento.addEventListener("hidden.bs.modal", () => {
                elemento.remove();
                resolve(confirmado);
            });
            modal.show();
        });
    }

    /**
     * Renderiza os controles "Anterior / Pagina X de Y / Proxima" a partir do
     * PaginaDTO que a API devolve, e liga o clique a um callback que recarrega
     * a lista com a pagina escolhida. Usado por toda tela com lista paginada.
     */
    function montarPaginacao(elementoContainer, pagina, aoTrocarPagina) {
        elementoContainer.innerHTML = "";
        if (pagina.totalPaginas <= 1) {
            return;
        }

        const criarBotao = (texto, numeroPagina, ativo, desabilitado) => {
            const botao = document.createElement("button");
            botao.type = "button";
            botao.className = `btn btn-sm ${ativo ? "btn-ovg-principal" : "btn-ovg-secundario"}`;
            botao.textContent = texto;
            botao.disabled = Boolean(desabilitado);
            if (!desabilitado && !ativo) {
                botao.addEventListener("click", () => aoTrocarPagina(numeroPagina));
            }
            return botao;
        };

        const botoes = document.createElement("div");
        botoes.className = "d-flex gap-1 flex-wrap justify-content-center";
        botoes.appendChild(criarBotao("« Anterior", pagina.paginaAtual - 1, false, pagina.paginaAtual === 0));

        numerosDePaginaParaMostrar(pagina.paginaAtual, pagina.totalPaginas)
            .map((numero) => {
                if (numero === null) {
                    const reticencias = document.createElement("span");
                    reticencias.className = "px-1 text-muted";
                    reticencias.textContent = "…";
                    return reticencias;
                }
                return criarBotao(String(numero + 1), numero, numero === pagina.paginaAtual, false);
            })
            .forEach((elemento) => botoes.appendChild(elemento));

        botoes.appendChild(criarBotao("Proxima »", pagina.paginaAtual + 1, false, pagina.paginaAtual + 1 >= pagina.totalPaginas));

        const legenda = document.createElement("div");
        legenda.className = "text-muted small text-center mt-1";
        legenda.textContent = `Pagina ${pagina.paginaAtual + 1} de ${pagina.totalPaginas} · ${pagina.totalElementos} no total`;

        const nav = document.createElement("div");
        nav.className = "d-flex flex-column align-items-center mt-3";
        nav.appendChild(botoes);
        nav.appendChild(legenda);
        elementoContainer.appendChild(nav);
    }

    // indices (0-based) das paginas a mostrar: primeira, ultima e uma janela ao
    // redor da atual — "null" marca onde entra uma reticencia "..." no meio
    function numerosDePaginaParaMostrar(paginaAtual, totalPaginas) {
        const janela = 2;
        const paginas = new Set([0, totalPaginas - 1]);
        for (let i = paginaAtual - janela; i <= paginaAtual + janela; i++) {
            if (i >= 0 && i < totalPaginas) {
                paginas.add(i);
            }
        }

        const ordenadas = [...paginas].sort((a, b) => a - b);
        const resultado = [];
        ordenadas.forEach((numero, indice) => {
            if (indice > 0 && numero - ordenadas[indice - 1] > 1) {
                resultado.push(null);
            }
            resultado.push(numero);
        });
        return resultado;
    }

    return { mostrarToast, confirmarAcao, montarPaginacao };
})();
