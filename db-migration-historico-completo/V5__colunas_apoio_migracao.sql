-- Colunas temporarias usadas apenas durante a migracao dos dados do sistema
-- legado: permitem religar registros novos aos registros de origem (para
-- validacao e para as proximas migrations) e preservar textos que nao
-- puderam ser estruturados automaticamente. Devem ser removidas numa
-- migration futura, manualmente, depois que a migracao for validada
-- (ver README > "Migracao de dados historicos").

ALTER TABLE veiculo ADD COLUMN id_legado INT NULL;
ALTER TABLE motorista ADD COLUMN id_legado INT NULL;
ALTER TABLE usuario ADD COLUMN login_gerado_na_migracao BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE solicitacao ADD COLUMN id_legado INT NULL;
ALTER TABLE solicitacao ADD COLUMN origem_texto_legado VARCHAR(400) NULL;
ALTER TABLE solicitacao ADD COLUMN destino_texto_legado VARCHAR(400) NULL;
ALTER TABLE viagem ADD COLUMN id_legado INT NULL;

CREATE INDEX idx_solicitacao_id_legado ON solicitacao(id_legado);
CREATE INDEX idx_viagem_id_legado ON viagem(id_legado);
