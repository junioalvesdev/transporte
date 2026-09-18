/**
 * Camada fina sobre fetch: centraliza a URL base, os headers padrao e o
 * tratamento de erro (ApiErrorResponse do backend), para as telas nao
 * repetirem esse boilerplate a cada chamada.
 */
const Api = (() => {
    async function requisitar(caminho, opcoes = {}) {
        const resposta = await fetch(caminho, {
            ...opcoes,
            headers: { "Content-Type": "application/json", ...(opcoes.headers || {}) },
            credentials: "same-origin",
        });

        if (resposta.status === 401 && !window.location.pathname.endsWith("login.html")) {
            window.location.href = "login.html";
            // a pagina esta de saida (redirecionando) — devolve uma Promise que
            // nunca resolve, pra nenhum "await Api.get(...)" espalhado pelas
            // telas continuar rodando com um valor null e quebrar com
            // "Cannot read properties of null" antes do navegador trocar de pagina.
            return new Promise(() => {});
        }

        if (!resposta.ok) {
            const erro = await resposta.json().catch(() => ({ mensagem: "Erro inesperado" }));
            // `detalhes` traz o motivo campo a campo (ex.: "data: deve ser uma
            // data futura ou presente") — sem isso, todo erro de validacao
            // aparece so como "Dados invalidos", sem dar pra saber o porque.
            const detalhes = Array.isArray(erro.detalhes) && erro.detalhes.length > 0
                ? ` (${erro.detalhes.join("; ")})`
                : "";
            throw new Error((erro.mensagem || "Erro inesperado") + detalhes);
        }

        if (resposta.status === 204) {
            return null;
        }
        return resposta.json();
    }

    return {
        get: (caminho) => requisitar(caminho),
        post: (caminho, corpo) => requisitar(caminho, { method: "POST", body: JSON.stringify(corpo) }),
        put: (caminho, corpo) => requisitar(caminho, { method: "PUT", body: JSON.stringify(corpo) }),
        patch: (caminho, corpo) => requisitar(caminho, { method: "PATCH", body: corpo ? JSON.stringify(corpo) : undefined }),
        delete: (caminho) => requisitar(caminho, { method: "DELETE" }),
    };
})();
