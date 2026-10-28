# Painel administrativo — Talentos do Capão

Nova interface administrativa em Angular, TypeScript e Tailwind CSS. Esta aplicação substitui gradualmente o frontend estático, que permanece disponível no diretório `../Gerenciador-de-Alunos-Front`.

## Requisitos

- Node.js 22 LTS
- npm 10 ou superior
- Backend Spring Boot disponível em `http://localhost:8080`

## Desenvolvimento

```bash
npm install
npm start
```

Abra `http://localhost:4200`. O servidor de desenvolvimento encaminha chamadas de `/api` ao backend por meio de `proxy.conf.json`, evitando URLs locais espalhadas pelo código e dispensando CORS no desenvolvimento.

## Validação

```bash
npm test
npm run build
```

## Autenticação

- `POST /api/auth/login` recebe `username` e `password`.
- O JWT é mantido no `sessionStorage`, portanto não sobrevive ao fechamento da aba.
- `GET /api/auth/me` revalida o usuário e suas roles ao restaurar a sessão.
- O interceptor adiciona `Authorization: Bearer <token>` somente às chamadas da API.
- Respostas `401` encerram a sessão; respostas `403` redirecionam à página de acesso negado.

O frontend usa guards e oculta opções sem permissão apenas para melhorar a experiência. A autorização efetiva continua sendo responsabilidade do backend.

## Estrutura

```text
src/app/
  core/          autenticação, guards, interceptors e configuração de navegação
  shared/        modelos e componentes reutilizáveis
  layout/        shell administrativo, sidebar e header
  features/      login, dashboard, alunos e páginas de erro
```

Os módulos de turmas, usuários, auditoria, configurações e relatórios ainda não foram implementados. A matriz de navegação já reserva as permissões futuras sem expor rotas inexistentes.

## Deploy sob `/admin`

Caso o painel seja publicado no mesmo domínio sob `/admin`, gere o build com:

```bash
npm run build -- --base-href /admin/
```

O servidor web deve encaminhar `/api` ao backend e devolver `index.html` para as rotas do Angular. Em um subdomínio dedicado, mantenha o mesmo encaminhamento reverso para `/api` ou configure CORS com origens explícitas no backend.
