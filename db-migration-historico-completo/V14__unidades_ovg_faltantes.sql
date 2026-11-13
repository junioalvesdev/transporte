-- Legado tinha 12 unidades cadastradas (tabela `endereco`); so 3 (SEDE, CISF,
-- CIGO) foram migradas em V2. Completa as demais, todas em Goiania.
INSERT INTO local_administrativo (nome, cidade_id)
SELECT unidade.nome, cidade_goiania.id
FROM (
    SELECT 'CHEFATURA' AS nome UNION ALL
    SELECT 'CIVV' UNION ALL
    SELECT 'EBV1' UNION ALL
    SELECT 'EBV2' UNION ALL
    SELECT 'EBV3' UNION ALL
    SELECT 'GBA' UNION ALL
    SELECT 'GRB' UNION ALL
    SELECT 'PJTF' UNION ALL
    SELECT 'PML'
) AS unidade
JOIN (
    SELECT c.id
    FROM cidade c
    JOIN estado e ON e.id = c.estado_id AND e.uf = 'GO'
    WHERE c.nome = 'Goiânia'
) AS cidade_goiania ON TRUE;
