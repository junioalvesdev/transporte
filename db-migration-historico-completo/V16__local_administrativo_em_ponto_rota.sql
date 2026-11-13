-- PontoRota so guardava a cidade; unidades diferentes na mesma cidade (ex.:
-- SEDE -> CISF, ambas em Goiania) ficavam indistinguiveis na exibicao da
-- viagem. Guarda a unidade tambem, quando a rota veio de uma unidade.
ALTER TABLE ponto_rota ADD COLUMN local_administrativo_id BIGINT NULL;
ALTER TABLE ponto_rota ADD CONSTRAINT fk_ponto_rota_local FOREIGN KEY (local_administrativo_id) REFERENCES local_administrativo(id);
