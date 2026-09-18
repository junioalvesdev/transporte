# Como rodar

Você precisa ter o Docker instalado. Depois disso:

```bash
docker compose up --build
```

Isso sobe três coisas:
- o sistema em `http://localhost:8080`
- o banco de dados MySQL em `localhost:3306`
- o Adminer (uma telinha simples pra olhar o banco pelo navegador) em
  `http://localhost:8081`

O banco sobe vazio — o Flyway cria todas as tabelas e alguns dados básicos
automaticamente (tipos de veículo, cidades do catálogo do IBGE, as unidades
da OVG). Não tem nenhum dado real de pessoa nenhuma — isso é proposital, ver
a seção "Sobre os dados" no `README.md`.

## Entrando sem precisar de um Active Directory

Como o banco sobe vazio (sem nenhum usuário do Active Directory cadastrado),
o `docker-compose.yml` já vem com um login "Normal" habilitado: usuário e
senha fixos `admin` / `admin`. Na tela de login, clique na aba "Normal
(teste)" e entre com esse usuário.

## Se você quiser usar o LDAP de verdade

O arquivo `docker-compose.yml` tem duas variáveis de ambiente em branco de
propósito — `LDAP_URL` e `LDAP_DOMINIO`. Preencha com o endereço do seu
servidor Active Directory antes de subir:

```bash
LDAP_URL=ldap://seu-servidor LDAP_DOMINIO=suaempresa.com.br docker compose up --build
```

Nesse caso vale a pena também desligar o login de teste, apagando ou
mudando pra `false` a variável `AUTH_TESTE_HABILITADO` no `docker-compose.yml`.

## Estrutura do projeto

```
transporte-novo/
├── src/main/java/com/ovg/transportes/
│   ├── controller/     # os endpoints da API (o que o frontend chama)
│   ├── service/        # as regras de negocio (aprovar solicitacao, etc.)
│   ├── repository/      # acesso ao banco (Spring Data JPA)
│   ├── model/           # as entidades (Solicitacao, Viagem, Veiculo...)
│   ├── dto/              # os objetos que trafegam entre frontend e API
│   ├── security/         # login via LDAP e o login de teste
│   ├── config/           # configuracoes gerais do Spring
│   └── common/           # excecoes e classes compartilhadas
│
├── src/main/resources/
│   ├── application.yml           # todas as configuracoes (banco, LDAP, etc.)
│   ├── db/migration/              # como o banco e criado (Flyway)
│   │   ├── V1__schema_inicial.sql    # todas as tabelas
│   │   └── V2__dados_iniciais.sql    # catalogo IBGE, unidades da OVG, login de teste
│   └── static/                   # o frontend inteiro (HTML, CSS, JS)
│       ├── index.html            # tela principal (buscar viagem, solicitar)
│       ├── calendario.html       # calendario do Departamento de Transportes
│       ├── solicitacoes-pendentes.html
│       ├── veiculos.html / motoristas.html
│       └── js/                   # um arquivo JS por tela/funcionalidade
│
├── Dockerfile                    # como a aplicacao vira uma imagem Docker
└── docker-compose.yml            # como subir o sistema (banco vazio, login de teste)
```
