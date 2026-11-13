-- Cataogos de dominio pequenos: perfis do sistema e tipos de veiculo, este
-- ultimo levantado dos valores distintos usados em transportes2.sql (tabela veiculo).

INSERT INTO perfil (codigo) VALUES ('FUNCIONARIO'), ('TRANSPORTE'), ('ADMINISTRADOR');

INSERT INTO local_administrativo (nome) VALUES ('SEDE'), ('CISF'), ('CIGO');

INSERT INTO tipo_veiculo (nome) VALUES
    ('Carro'), ('Moto'), ('Onibus'), ('Kombi'), ('Van'),
    ('Minibus'), ('Caminhao'), ('Ambulancia'), ('Furgao'), ('Caminhonete');
