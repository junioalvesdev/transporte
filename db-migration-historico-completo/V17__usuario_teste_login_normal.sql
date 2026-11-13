-- Usuario fixo usado pelo login "Normal" (admin/admin) quando
-- transportes.auth-teste.habilitado=true — sem essa flag ligada, essa linha
-- nunca e usada pra autenticar ninguem (ver TesteAuthenticationProvider).
INSERT INTO usuario (login_ad, nome, email, ativo, login_gerado_na_migracao)
SELECT 'admin.teste', 'Administrador de Teste', 'admin.teste@local', TRUE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE login_ad = 'admin.teste');

INSERT INTO usuario_perfil (usuario_id, perfil_id)
SELECT u.id, p.id
FROM usuario u, perfil p
WHERE u.login_ad = 'admin.teste'
  AND p.codigo = 'TRANSPORTE'
  AND NOT EXISTS (SELECT 1 FROM usuario_perfil up WHERE up.usuario_id = u.id AND up.perfil_id = p.id);
