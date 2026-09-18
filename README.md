# Sistema de Transportes da OVG

Esse projeto é uma reescrita completa do sistema de gestão de transporte da
OVG (Organização das Voluntárias de Goiás). O sistema antigo era em PHP; esse
aqui é novo, feito do zero em Java, mantendo as mesmas regras de negócio mas
com uma arquitetura mais organizada e algumas funcionalidades novas.

## O que o sistema faz

No dia a dia, um funcionário da OVG que precisa de um transporte abre o
sistema, escolhe a unidade de origem e destino (ou descreve o endereço, se
não for uma unidade), diz quando precisa sair e volta, e envia a solicitação.
O Departamento de Transportes recebe essa solicitação, escolhe um veículo e
um motorista disponíveis, e aprova (ou reprova, com um motivo). A partir daí
vira uma viagem de verdade, que aparece no calendário do Departamento.

Uma coisa que o sistema antigo não tinha e esse tem: se duas pessoas
solicitam transporte pro mesmo lugar, no mesmo horário, elas podem dividir o
mesmo veículo — o sistema controla quantas vagas ainda tem disponíveis em
cada viagem automaticamente.

O Departamento de Transportes também tem uma tela de calendário (mês/semana)
pra ver todas as viagens programadas, uma tela pra gerenciar veículos e
motoristas, e consegue gerar em PDF a "Ordem de Tráfego" — o documento formal
que autoriza aquele motorista a sair com aquele veículo num dia específico,
igual o sistema antigo fazia.

## Tecnologias usadas

- **Backend**: Java 21 + Spring Boot (Spring Data JPA, Spring Security)
- **Banco de dados**: MySQL 8, com as mudanças de schema controladas por
  Flyway (cada alteração no banco vira um arquivo versionado, nunca editado
  depois de aplicado)
- **Frontend**: HTML + JavaScript puro + Bootstrap — sem framework, sem passo
  de build, os arquivos são servidos direto pelo Spring Boot
- **Login**: autenticação via Active Directory (LDAP) — a pessoa usa o mesmo
  usuário e senha que já usa no computador da empresa
- **PDF**: geração da Ordem de Tráfego via `openhtmltopdf` (HTML/CSS vira PDF)
- **Docker**: a aplicação inteira (app + banco + visualizador de banco) sobe
  com um único comando

## Como rodar

Instruções completas (incluindo como testar sem precisar de um Active
Directory de verdade, e a estrutura de pastas do projeto) estão em
[COMO-RODAR.md](COMO-RODAR.md).

Resumindo:

```bash
docker compose up --build
```

## Sobre os dados (por que esse repositório é seguro de publicar)

O sistema antigo tinha mais de 40 mil solicitações reais, com nomes,
telefones e e-mails de gente de verdade. Pra migrar esse histórico pro banco
novo, em algum momento foram usadas migrations do Flyway com esses dados reais
dentro — mas essas migrations **nunca estiveram neste repositório**. Elas só
existiram numa pasta separada, fora do controle de versão, numa máquina local.

O que está no git (`src/main/resources/db/migration/`) é só duas coisas: o
schema (estrutura das tabelas) e dados genéricos sem nenhuma informação
pessoal — tipos de veículo, o catálogo de cidades do IBGE, as unidades da
OVG, e alguns registros fictícios de exemplo (marcados como "(exemplo)" no
próprio texto) pra já ter algo pra ver na tela assim que o sistema sobe.

Um clone novo desse repositório sobe funcionando perfeitamente, com esses
dados de exemplo — nunca com uma solicitação, veículo ou usuário real.
