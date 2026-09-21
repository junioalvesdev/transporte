/**
 * Bloqueia paginas exclusivas do Departamento de Transportes (Veiculos,
 * Motoristas, Relatorios, Solicitacoes pendentes) pra quem nao tem perfil
 * TRANSPORTE — a API ja recusa essas chamadas com 403, isso aqui e so pra
 * nao deixar a tela quebrada visualmente antes do redirecionamento.
 */
document.addEventListener("DOMContentLoaded", async () => {
    const perfil = await window.perfilUsuarioPromise;
    if (!perfil.ehTransporte) {
        window.location.href = "calendario.html";
    }
});
