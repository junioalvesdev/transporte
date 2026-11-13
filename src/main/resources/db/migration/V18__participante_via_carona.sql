-- Marca quem entrou numa viagem por conta propria pela busca de caronas
-- ("Solicitar vaga"), pra o Departamento de Transportes distinguir no
-- detalhe da viagem quem pediu a viagem de quem pegou carona nela depois.
ALTER TABLE viagem_participante
    ADD COLUMN via_carona BOOLEAN NOT NULL DEFAULT FALSE AFTER qtd_passageiros;

-- Caronas que ja existiam antes desta coluna: a solicitacao criada pelo
-- "Solicitar vaga" nasce sem unidade administrativa, sem retorno e com a
-- data igual a saida da viagem, e nunca e o primeiro participante (esse e
-- quem originou a viagem). Solicitacoes migradas do legado ficam de fora.
UPDATE viagem_participante vp
JOIN solicitacao s ON s.id = vp.solicitacao_id
JOIN viagem v ON v.id = vp.viagem_id
JOIN (
    SELECT viagem_id, MIN(id) AS primeiro_id
    FROM viagem_participante
    GROUP BY viagem_id
) primeiro ON primeiro.viagem_id = vp.viagem_id
SET vp.via_carona = TRUE
WHERE vp.id <> primeiro.primeiro_id
  AND s.id_legado IS NULL
  AND s.local_origem_id IS NULL
  AND s.local_destino_id IS NULL
  AND s.data_hora_retorno_desejada IS NULL
  AND s.data_hora_desejada = v.data_hora_saida;
