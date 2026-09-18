/**
 * Navbar compartilhada das telas do Departamento de Transportes: um menu
 * suspenso com todas as secoes, sempre visivel — evita ter que voltar pro
 * hub (admin.html) toda vez que precisa trocar de secao.
 */
document.addEventListener("DOMContentLoaded", () => {
    const alvo = document.getElementById("nav-admin-container");
    if (!alvo) {
        return;
    }

    const paginaAtual = window.location.pathname.split("/").pop();
    const secoes = [
        { href: "solicitacoes-pendentes.html", rotulo: "Solicitacoes pendentes" },
        { href: "veiculos.html", rotulo: "Veiculos" },
        { href: "motoristas.html", rotulo: "Motoristas" },
        { href: "calendario.html", rotulo: "Calendario" },
        { href: "relatorios.html", rotulo: "Relatorios" },
    ];

    const itens = secoes
        .map((secao) => `<li><a class="dropdown-item${secao.href === paginaAtual ? " active" : ""}" href="${secao.href}">${secao.rotulo}</a></li>`)
        .join("");

    alvo.innerHTML = `
        <a href="index.html" class="navbar-brand">Central de Transportes &middot; OVG</a>
        <div class="dropdown">
            <button class="btn btn-outline-light btn-sm dropdown-toggle" type="button" data-bs-toggle="dropdown">
                Departamento de Transportes
            </button>
            <ul class="dropdown-menu dropdown-menu-end">
                ${itens}
            </ul>
        </div>
    `;
});
