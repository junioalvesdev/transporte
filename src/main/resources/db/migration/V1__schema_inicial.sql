-- Schema completo do Sistema de Gestao de Transportes.
-- Charset unico (utf8mb4) em todas as tabelas.

CREATE TABLE perfil (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE departamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sigla VARCHAR(60) NOT NULL UNIQUE,
    nome VARCHAR(150) NOT NULL,
    permite_urgencia BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE estado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_uf INT NOT NULL UNIQUE,
    nome VARCHAR(50) NOT NULL,
    uf VARCHAR(2) NOT NULL UNIQUE,
    regiao INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cidade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_ibge INT NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    estado_id BIGINT NOT NULL,
    CONSTRAINT fk_cidade_estado FOREIGN KEY (estado_id) REFERENCES estado(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_cidade_nome ON cidade(nome);

-- cada unidade da OVG tem uma cidade "de casa" (ex.: SEDE -> Goiania), pra
-- alimentar o motor de compatibilidade de rota mesmo quando o usuario so
-- escolhe a unidade no formulario, sem precisar buscar cidade manualmente.
CREATE TABLE local_administrativo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    cidade_id BIGINT NOT NULL,
    CONSTRAINT fk_local_administrativo_cidade FOREIGN KEY (cidade_id) REFERENCES cidade(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_ad VARCHAR(100) NOT NULL UNIQUE,
    nome VARCHAR(200) NOT NULL,
    email VARCHAR(150) NOT NULL,
    telefone VARCHAR(30),
    departamento_id BIGINT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    -- marca contas cujo login foi aproximado a partir do e-mail (dado historico
    -- sem login de AD confiavel) — precisam ser conferidas manualmente
    login_gerado_na_migracao BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_usuario_departamento FOREIGN KEY (departamento_id) REFERENCES departamento(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuario_perfil (
    usuario_id BIGINT NOT NULL,
    perfil_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, perfil_id),
    CONSTRAINT fk_usuario_perfil_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_usuario_perfil_perfil FOREIGN KEY (perfil_id) REFERENCES perfil(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tipo_veiculo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE veiculo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    placa VARCHAR(10) NOT NULL UNIQUE,
    modelo VARCHAR(150) NOT NULL,
    capacidade INT NOT NULL,
    tipo_veiculo_id BIGINT NOT NULL,
    local_id BIGINT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_por VARCHAR(100),
    criado_em DATETIME,
    atualizado_por VARCHAR(100),
    atualizado_em DATETIME,
    id_legado INT,
    CONSTRAINT fk_veiculo_tipo FOREIGN KEY (tipo_veiculo_id) REFERENCES tipo_veiculo(id),
    CONSTRAINT fk_veiculo_local FOREIGN KEY (local_id) REFERENCES local_administrativo(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE motorista (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(30),
    local_id BIGINT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_por VARCHAR(100),
    criado_em DATETIME,
    atualizado_por VARCHAR(100),
    atualizado_em DATETIME,
    id_legado INT,
    CONSTRAINT fk_motorista_local FOREIGN KEY (local_id) REFERENCES local_administrativo(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- origem/destino aceitam OU uma unidade da OVG (local_origem_id/local_destino_id)
-- OU uma cidade buscada + descricao livre (cidade sempre preenchida, e o
-- "backbone" que o motor de compatibilidade de rota entende; descricao so
-- quando o usuario nao escolheu uma unidade) — nunca as duas, nunca nenhuma,
-- validado na aplicacao.
CREATE TABLE solicitacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitante_id BIGINT NOT NULL,
    cidade_origem_id BIGINT NOT NULL,
    cidade_destino_id BIGINT NOT NULL,
    local_origem_id BIGINT,
    local_destino_id BIGINT,
    descricao_origem VARCHAR(400),
    descricao_destino VARCHAR(400),
    data_hora_desejada DATETIME NOT NULL,
    data_hora_retorno_desejada DATETIME,
    qtd_passageiros INT NOT NULL,
    telefone_contato VARCHAR(20),
    finalidade VARCHAR(200),
    observacoes VARCHAR(500),
    urgente BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,
    motivo_recusa VARCHAR(500),
    criado_por VARCHAR(100),
    criado_em DATETIME,
    atualizado_por VARCHAR(100),
    atualizado_em DATETIME,
    id_legado INT,
    origem_texto_legado VARCHAR(400),
    destino_texto_legado VARCHAR(400),
    CONSTRAINT fk_solicitacao_solicitante FOREIGN KEY (solicitante_id) REFERENCES usuario(id),
    CONSTRAINT fk_solicitacao_origem FOREIGN KEY (cidade_origem_id) REFERENCES cidade(id),
    CONSTRAINT fk_solicitacao_destino FOREIGN KEY (cidade_destino_id) REFERENCES cidade(id),
    CONSTRAINT fk_solicitacao_local_origem FOREIGN KEY (local_origem_id) REFERENCES local_administrativo(id),
    CONSTRAINT fk_solicitacao_local_destino FOREIGN KEY (local_destino_id) REFERENCES local_administrativo(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_solicitacao_status_data ON solicitacao(status, data_hora_desejada);
CREATE INDEX idx_solicitacao_solicitante ON solicitacao(solicitante_id);
CREATE INDEX idx_solicitacao_id_legado ON solicitacao(id_legado);

CREATE TABLE viagem (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    veiculo_id BIGINT NOT NULL,
    motorista_id BIGINT NOT NULL,
    data_hora_saida DATETIME NOT NULL,
    data_hora_chegada_estimada DATETIME,
    capacidade_total INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    criado_por VARCHAR(100),
    criado_em DATETIME,
    atualizado_por VARCHAR(100),
    atualizado_em DATETIME,
    id_legado INT,
    CONSTRAINT fk_viagem_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculo(id),
    CONSTRAINT fk_viagem_motorista FOREIGN KEY (motorista_id) REFERENCES motorista(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_viagem_status_data ON viagem(status, data_hora_saida);
CREATE INDEX idx_viagem_veiculo_data ON viagem(veiculo_id, data_hora_saida);
CREATE INDEX idx_viagem_motorista_data ON viagem(motorista_id, data_hora_saida);
CREATE INDEX idx_viagem_id_legado ON viagem(id_legado);

CREATE TABLE rota (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    viagem_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_rota_viagem FOREIGN KEY (viagem_id) REFERENCES viagem(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- local_administrativo_id e opcional: so preenchido quando o ponto veio de
-- uma unidade da OVG (nao de uma cidade "outro local") — da precisao na
-- exibicao quando origem e destino sao unidades diferentes na mesma cidade.
CREATE TABLE ponto_rota (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rota_id BIGINT NOT NULL,
    cidade_id BIGINT NOT NULL,
    local_administrativo_id BIGINT,
    ordem INT NOT NULL,
    tipo_ponto VARCHAR(20) NOT NULL,
    horario_estimado DATETIME,
    CONSTRAINT fk_ponto_rota_rota FOREIGN KEY (rota_id) REFERENCES rota(id),
    CONSTRAINT fk_ponto_rota_cidade FOREIGN KEY (cidade_id) REFERENCES cidade(id),
    CONSTRAINT fk_ponto_rota_local FOREIGN KEY (local_administrativo_id) REFERENCES local_administrativo(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_ponto_rota_rota_ordem ON ponto_rota(rota_id, ordem);

CREATE TABLE viagem_participante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    viagem_id BIGINT NOT NULL,
    solicitacao_id BIGINT NOT NULL UNIQUE,
    qtd_passageiros INT NOT NULL,
    cidade_embarque_id BIGINT NOT NULL,
    cidade_desembarque_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_participante_viagem FOREIGN KEY (viagem_id) REFERENCES viagem(id),
    CONSTRAINT fk_participante_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES solicitacao(id),
    CONSTRAINT fk_participante_embarque FOREIGN KEY (cidade_embarque_id) REFERENCES cidade(id),
    CONSTRAINT fk_participante_desembarque FOREIGN KEY (cidade_desembarque_id) REFERENCES cidade(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_participante_viagem ON viagem_participante(viagem_id);

CREATE TABLE avaliacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    viagem_participante_id BIGINT NOT NULL UNIQUE,
    nota INT NOT NULL,
    categoria VARCHAR(100),
    comentario VARCHAR(500),
    CONSTRAINT fk_avaliacao_participante FOREIGN KEY (viagem_participante_id) REFERENCES viagem_participante(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    status_tratamento VARCHAR(20) NOT NULL DEFAULT 'ABERTO',
    criado_em DATETIME NOT NULL,
    CONSTRAINT fk_feedback_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE auditoria_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entidade VARCHAR(60) NOT NULL,
    entidade_id BIGINT NOT NULL,
    acao VARCHAR(40) NOT NULL,
    usuario_id BIGINT NOT NULL,
    dados_antes JSON,
    dados_depois JSON,
    criado_em DATETIME NOT NULL,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_auditoria_entidade ON auditoria_log(entidade, entidade_id);
