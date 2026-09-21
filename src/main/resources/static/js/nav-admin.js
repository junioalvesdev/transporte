/**
 * Navbar compartilhada de todas as telas: um menu suspenso com as secoes —
 * o Calendario e a pagina principal (busca de viagens e a solicitacao de nova
 * reserva vivem la, no proprio cabecalho e no botao "Criar Reservas"). Todo
 * usuario logado ve Calendario e Minhas solicitacoes; Solicitacoes pendentes,
 * Veiculos, Motoristas e Relatorios so aparecem pra quem tem perfil TRANSPORTE
 * (a API tambem recusa essas chamadas pra quem nao tem o perfil — isso aqui e
 * so pra nao mostrar um link que vai dar 403).
 */
document.addEventListener("DOMContentLoaded", async () => {
    const alvo = document.getElementById("nav-admin-container");
    if (!alvo) {
        return;
    }

    const perfil = await window.perfilUsuarioPromise;
    const paginaAtual = window.location.pathname.split("/").pop();
    const ehCalendario = paginaAtual === "calendario.html" || paginaAtual === "";
    const ehMinhasSolicitacoes = ehCalendario && window.location.hash === "#minhas";

    const secoesTodoMundo = [
        { href: "calendario.html", rotulo: "Calendario", ativo: ehCalendario && !ehMinhasSolicitacoes },
        { href: "calendario.html#minhas", rotulo: "Minhas solicitacoes", ativo: ehMinhasSolicitacoes },
    ];
    const secoesTransporte = [
        { href: "reservas.html", rotulo: "Lista de Reservas", ativo: paginaAtual === "reservas.html" },
        { href: "solicitacoes-pendentes.html", rotulo: "Solicitacoes pendentes", ativo: paginaAtual === "solicitacoes-pendentes.html" },
        { href: "veiculos.html", rotulo: "Veiculos", ativo: paginaAtual === "veiculos.html" },
        { href: "motoristas.html", rotulo: "Motoristas", ativo: paginaAtual === "motoristas.html" },
        { href: "relatorios.html", rotulo: "Relatorios", ativo: paginaAtual === "relatorios.html" },
    ];
    const secoes = perfil.ehTransporte ? [...secoesTodoMundo, ...secoesTransporte] : secoesTodoMundo;

    const itens = secoes
        .map((secao) => `<li><a class="dropdown-item${secao.ativo ? " active" : ""}" href="${secao.href}">${secao.rotulo}</a></li>`)
        .join("");

    alvo.innerHTML = `
        <a href="calendario.html" class="navbar-brand">Central de Transportes</a>
        <div class="d-flex gap-2">
            <div class="dropdown">
                <button class="btn btn-outline-light btn-sm dropdown-toggle" type="button" data-bs-toggle="dropdown">
                    ${perfil.ehTransporte ? "Departamento de Transportes" : "Menu"}
                </button>
                <ul class="dropdown-menu dropdown-menu-end">
                    ${itens}
                </ul>
            </div>
            <button class="btn btn-sm btn-outline-light" id="botao-sair">Sair</button>
        </div>
    `;

    // encerra a sessao no backend e manda de volta pro portal antigo — nao usa
    // Api.post porque /api/auth/logout nao devolve corpo JSON nenhum (so
    // invalida a sessao), e o redirecionamento acontece de qualquer jeito
    // mesmo se a chamada falhar (sessao ja expirada, por exemplo)
    document.getElementById("botao-sair").addEventListener("click", async () => {
        try {
            await fetch("/api/auth/logout", { method: "POST", credentials: "same-origin" });
        } finally {
            window.location.href = "http://10.237.2.35/";
        }
    });
});
