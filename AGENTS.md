# AGENTS.md — Gerenciador de Alunos

## Objetivo do projeto

Sistema da ONG Talentos do Capão para cadastrar e gerenciar alunos. O sistema
atualmente mantém dados básicos do aluno (nome, e-mail, CPF, telefone e foto),
com consulta e exclusão. Mudanças futuras devem preservar os fluxos existentes
e evoluir o sistema gradualmente, sem reescrever o projeto do zero.

## Stack

- Backend: Java 21, Spring Boot, Maven.
- API: Spring Web, Spring Data JPA, Bean Validation e Lombok.
- Banco de produção/desenvolvimento: MySQL 8.
- Banco de testes: H2 em memória.
- Frontend: HTML estático, JavaScript puro e Tailwind CSS via CDN.
- Uploads: armazenamento local no diretório configurado por `file.upload-dir`.

## Arquitetura atual

```text
Gerenciador-de-Alunos-Front (HTML/JavaScript)
        -> API REST Spring Boot (/api/clients)
        -> Controller -> Service -> Repository -> MySQL
        -> /uploads/** -> arquivos locais de foto
```

O frontend e o backend são aplicações separadas. O frontend chama a API por
`fetch` e usa `multipart/form-data` para cadastro com foto.

## Estrutura de diretórios

```text
Gerenciador-de-Alunos-Front/       frontend estático
  index.html                       cadastro e início da busca
  details.html                     resultado da busca e exclusão

Gerenciador-de-Alunos/             backend Maven/Spring Boot
  src/main/java/.../controller/    rotas HTTP
  src/main/java/.../service/       regras de negócio
  src/main/java/.../repository/    acesso JPA ao banco
  src/main/java/.../model/         entidades JPA
  src/main/java/.../dto/           dados de entrada da API
  src/main/java/.../exception/     erros de domínio e handler global
  src/main/java/.../configuration/ configurações web
  src/main/resources/              configuração da aplicação e Compose
  src/test/                        testes automatizados
```

## Padrões de código

- Use nomes alinhados ao domínio da ONG. Para código novo, prefira `Student`/
  `Aluno` a nomes genéricos como `Client`; não renomeie elementos existentes sem
  uma migração planejada.
- Mantenha a separação Controller -> Service -> Repository. Controllers não
  devem concentrar regras de negócio nem acesso direto ao repositório.
- Use DTOs para entradas e, ao ampliar a API, prefira DTOs também nas respostas
  para não expor entidades JPA diretamente.
- Valide entradas com Bean Validation e responda erros pelo handler global.
- Não duplique regras de validação, URLs, mensagens ou lógica entre páginas e
  classes quando uma abstração simples puder centralizá-las.
- Mantenha arquivos de texto em UTF-8. Não introduza arquivos com codificação
  ANSI/Windows-1252.

## Regras para frontend

- Preserve HTML, JavaScript e Tailwind enquanto não houver decisão explícita de
  adotar um framework.
- Centralize a configuração da URL da API antes de criar novas páginas ou fluxos.
- Sempre aplique `encodeURIComponent` a valores inseridos em URLs.
- Não insira dados fornecidos por usuários com `innerHTML`, atributos `onclick`
  montados por string ou JavaScript inline; use DOM APIs e `textContent` para
  evitar XSS.
- Trate respostas não bem-sucedidas de `fetch` e apresente mensagens úteis.
- Mantenha a interface em português do Brasil, acessível por teclado e com
  feedback de carregamento, sucesso e erro nos novos fluxos.
- Não exponha no frontend credenciais, segredos ou URLs específicas de produção.

## Regras para backend

- Mantenha rotas sob `/api` e use verbos HTTP e códigos de status adequados.
- Documente endpoints novos com OpenAPI/Swagger quando essa infraestrutura for
  introduzida; até lá, registre rotas e contratos no material do projeto.
- Para listas, implemente paginação, ordenação e filtros antes de expor volumes
  grandes de dados.
- Operações que alterem banco e arquivos devem considerar consistência e
  transações; não deixe registros e arquivos em estados divergentes.
- Uploads devem validar tamanho, tipo MIME e extensão, gerar nome seguro e nunca
  confiar no nome original fornecido pelo navegador.
- Não adicione dependências sem uso. Antes de incluir uma biblioteca, justifique
  seu propósito e mantenha o `pom.xml` enxuto.

## Regras para banco de dados

- MySQL é o banco da aplicação; H2 é exclusivo para testes.
- Não use `spring.jpa.hibernate.ddl-auto=update` em produção. Antes de uma
  implantação, adote migrações versionadas (preferencialmente Flyway ou
  Liquibase).
- Dados identificadores, como CPF, devem ter restrições e índices no banco, não
  somente verificações no serviço.
- Toda alteração de esquema deve ser compatível com dados existentes, ter plano
  de migração e reversão e ser testada.
- Não coloque credenciais no controle de versão. Use variáveis de ambiente ou
  arquivos locais ignorados pelo Git, com exemplos sem segredos.

## Regras de segurança e privacidade

- Dados de alunos são pessoais: aplique mínimo privilégio, não exponha listagens
  ou fotos publicamente sem necessidade e evite registrar dados sensíveis em log.
- Não amplie `@CrossOrigin(origins = "*")`; substitua por origens explícitas ao
  preparar ambientes reais.
- Novos recursos devem prever autenticação e autorização por perfil antes de
  serem disponibilizados a usuários externos.
- Valide e normalize CPF, e-mail, telefone e todos os dados recebidos pela API.
- Nunca devolva detalhes internos, stack traces, credenciais ou mensagens de
  banco ao cliente.
- Revise possíveis XSS, injeção, exposição de arquivos e controle de acesso em
  cada endpoint ou tela nova.

## Regras de testes e build

- Todo comportamento novo ou bug corrigido deve receber teste automatizado.
- Testes não devem depender do MySQL local, Docker, rede ou dados reais. Use o
  perfil `test` com H2 em memória.
- Execute `mvn test` antes de concluir uma alteração no backend.
- O projeto requer Java 21. Se o ambiente não tiver `JAVA_HOME` configurado,
  configure-o apenas para a sessão de execução em vez de alterar o projeto.
- O Maven Wrapper pode apresentar problemas no ambiente Windows atual; Maven
  instalado com Java 21 é uma alternativa aceitável para validação local.
- Para alterações de frontend sem ferramenta de build, valide o JavaScript e os
  fluxos afetados manualmente no navegador.

## Regras de Git

- Preserve alterações locais existentes que não pertençam à tarefa atual.
- Nunca use `git reset --hard`, `git checkout --` ou exclusões em massa sem
  solicitação explícita.
- Não versione `target/`, uploads reais, arquivos de IDE, credenciais, caches ou
  artefatos temporários.
- Faça commits pequenos, coerentes e com mensagens em português ou inglês que
  descrevam o efeito da alteração.
- Antes de um commit, revise `git diff` e confirme que não há segredos ou
  modificações acidentais.

## Regras para alterações futuras

1. Antes de uma alteração importante, explique objetivo, arquivos a modificar ou
   criar, impacto e riscos.
2. Implemente uma funcionalidade por vez; não misture refatorações amplas com
   mudanças funcionais sem necessidade.
3. Preserve os endpoints e fluxos em uso ou declare uma estratégia de
   compatibilidade/migração.
4. Execute testes e build proporcionais ao impacto, corrija falhas causadas pela
   alteração e informe limitações externas verificadas.
5. Não criar, alterar ou excluir dados de produção sem autorização explícita.
6. Ao concluir, informe o que mudou, como foi validado e o próximo passo lógico.
