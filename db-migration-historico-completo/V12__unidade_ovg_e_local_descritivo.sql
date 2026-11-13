-- Cada unidade da OVG passa a ter uma cidade "de casa" (SEDE/CISF/CIGO -> Goiania),
-- para o usuario poder escolher so a unidade no formulario de solicitacao e
-- ainda assim alimentar o motor de compatibilidade de rota (que so entende
-- cidade). Decisao registrada apos feedback de uso: a maioria das corridas e
-- local (uma unidade -> outra, ou um endereco especifico dentro da cidade),
-- nao intermunicipal, e forcar busca de cidade pra tudo era ruim.

ALTER TABLE local_administrativo ADD COLUMN cidade_id BIGINT NULL;

UPDATE local_administrativo la
JOIN cidade c ON c.nome = 'Goiânia'
JOIN estado e ON e.id = c.estado_id AND e.uf = 'GO'
SET la.cidade_id = c.id
WHERE la.nome IN ('SEDE', 'CISF', 'CIGO');

ALTER TABLE local_administrativo MODIFY COLUMN cidade_id BIGINT NOT NULL;
ALTER TABLE local_administrativo ADD CONSTRAINT fk_local_administrativo_cidade FOREIGN KEY (cidade_id) REFERENCES cidade(id);

-- Origem/destino da solicitacao agora aceitam unidade OU cidade+descricao
-- (nunca as duas, nunca nenhuma — validado na aplicacao, nao no banco).
ALTER TABLE solicitacao ADD COLUMN local_origem_id BIGINT NULL;
ALTER TABLE solicitacao ADD COLUMN local_destino_id BIGINT NULL;
ALTER TABLE solicitacao ADD COLUMN descricao_origem VARCHAR(400) NULL;
ALTER TABLE solicitacao ADD COLUMN descricao_destino VARCHAR(400) NULL;
ALTER TABLE solicitacao ADD CONSTRAINT fk_solicitacao_local_origem FOREIGN KEY (local_origem_id) REFERENCES local_administrativo(id);
ALTER TABLE solicitacao ADD CONSTRAINT fk_solicitacao_local_destino FOREIGN KEY (local_destino_id) REFERENCES local_administrativo(id);
