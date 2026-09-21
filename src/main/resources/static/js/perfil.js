/**
 * Carrega o perfil de quem esta logado uma unica vez por pagina — varias
 * telas precisam saber se e do Departamento de Transportes (ROLE_TRANSPORTE)
 * pra mostrar/esconder menu, botoes e campos de edicao. Comeca a buscar assim
 * que o script carrega (nao espera DOMContentLoaded), pra quem precisar do
 * resultado ter isso pronto o quanto antes. Precisa carregar depois de
 * api.js e antes de qualquer script que use window.perfilUsuarioPromise.
 */
window.perfilUsuarioPromise = Api.get("/api/auth/me").then((usuario) => ({
    login: usuario ? usuario.login : null,
    perfis: usuario ? usuario.perfis : [],
    ehTransporte: usuario ? usuario.perfis.includes("ROLE_TRANSPORTE") : false,
}));
