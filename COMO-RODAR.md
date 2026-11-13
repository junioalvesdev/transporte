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
automaticamente (tipos de veículo, cidades do catálogo do IBGE, algumas
unidades administrativas de exemplo). Não tem nenhum dado real de pessoa
nenhuma — isso é proposital, ver a seção "Sobre os dados" no `README.md`.

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
│   ├── controller/        # (C) os endpoints das paginas: recebem a requisicao,
│   │   │                  #     chamam o service e escolhem a tela (ex.: CalendarioController)
│   │   └── api/           # a API JSON (/api/...), para integracoes
│   ├── service/           # (M) as regras de negocio (SolicitacaoService, ViagemService...)
│   │   └── tela/          #     os services de cada tela: montam os dados prontos pra exibir
│   │                      #     (CalendarioService, ReservasService...) usando os de negocio
│   ├── repository/        # (M) acesso ao banco (Spring Data JPA)
│   ├── model/             # (M) as entidades (Solicitacao, Viagem, Veiculo...)
│   ├── dto/               # os objetos que trafegam entre as camadas
│   │   ├── solicitacao/ viagem/ veiculo/ motorista/ ...   # do sistema, por assunto
│   │   └── tela/          # os das telas, ja formatados: uma pasta por tela
│   │                      #     (calendario/, reservas/...) + layout/ e formularios/ compartilhados
│   ├── security/          # login via LDAP e o login de teste
│   ├── config/            # configuracoes gerais do Spring
│   └── common/            # excecoes e classes compartilhadas
│       └── tela/          #     formatacao, textos de status e validacao usados pelas telas
│
├── src/main/resources/
│   ├── application.yml           # todas as configuracoes (banco, LDAP, etc.)
│   ├── db/migration/              # como o banco e criado (Flyway)
│   ├── templates/                 # (V) as telas (Thymeleaf), uma pasta por tela
│   │   ├── fragmentos/            # pedacos comuns: layout (menu, avisos), modais compartilhados
│   │   ├── calendario/  reservas/  pendentes/  minhas/
│   │   ├── veiculos/  motoristas/  relatorios/
│   │   └── login.html
│   └── static/css/                # os estilos
│
├── Dockerfile                    # como a aplicacao vira uma imagem Docker
└── docker-compose.yml            # como subir o sistema (banco vazio, login de teste)
```

Como uma tela funciona (ex.: Veiculos): o navegador pede `GET /veiculos` →
`VeiculosController` chama `VeiculosService.montarLista()` → o service devolve
um DTO com tudo pronto pra exibir (textos formatados, opcoes marcadas) → o
controller poe no `Model` e escolhe `veiculos/lista` → o Thymeleaf preenche o
HTML e o navegador recebe a pagina pronta. Formularios sao POST comuns que
gravam e voltam pra tela com um aviso (Post/Redirect/Get).
