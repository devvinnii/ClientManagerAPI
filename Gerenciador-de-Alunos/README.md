# Gerenciador de Alunos

API para gerenciamento de alunos da ONG Talentos do Capão.

## Requisito de segurança: JWT_SECRET

A API exige a variável de ambiente `JWT_SECRET` em qualquer execução fora do
perfil de testes. Ela deve ser uma chave aleatória com ao menos 32 caracteres.
Ela não possui valor padrão propositalmente: a aplicação não inicia sem essa
configuração.

Não versione a chave. Use `.env.example` somente como referência e mantenha o
arquivo `.env` local fora do Git.

### PowerShell

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:JWT_EXPIRATION_MS = "3600000"
$taskDbCredential = Get-Credential -Message "Credenciais do MySQL de desenvolvimento"
$env:SPRING_DATASOURCE_USERNAME = $taskDbCredential.UserName
$env:SPRING_DATASOURCE_PASSWORD = $taskDbCredential.GetNetworkCredential().Password
```

Em seguida, execute a aplicação:

```powershell
mvn spring-boot:run
```

Esse modo usa `jdbc:mysql://localhost:3306/studentdb`. Use somente uma instância
local de desenvolvimento. Para outra URL, defina também `SPRING_DATASOURCE_URL`.

### Docker Compose

Crie um arquivo `.env` local a partir do exemplo, preencha os valores obrigatórios
e mantenha esse arquivo fora do Git. Use um nome de projeto exclusivo para que os
containers e volumes de desenvolvimento não se misturem com outros ambientes:

```powershell
Copy-Item .env.example .env
# Edite .env e defina JWT_SECRET, MYSQL_ROOT_PASSWORD, MYSQL_USER e MYSQL_PASSWORD.
docker compose --env-file .env -p gerenciador-alunos-dev `
  -f src/main/resources/docker-compose.yml up --build -d mysql api
```

Se as portas padrão já estiverem ocupadas, ajuste `API_PORT`, `MYSQL_PORT` e
`PHPMYADMIN_PORT` no `.env`. O backend acessa o MySQL pela rede interna do Compose,
independentemente da porta publicada no host. Os volumes `mysql_data` e
`uploads_data` são isolados pelo nome do projeto e preservam banco e fotos durante
reinicializações.

O serviço `api` recebe os segredos diretamente do ambiente. O Compose falha de
forma explícita quando `JWT_SECRET` ou as credenciais do MySQL não estão definidas.
Para encerrar os containers mantendo os dados locais:

```powershell
docker compose --env-file .env -p gerenciador-alunos-dev `
  -f src/main/resources/docker-compose.yml down
```

## Desenvolvimento

- Java 21
- MySQL 8 para desenvolvimento
- H2 em memória para testes

Execute os testes com `mvn test`.

## Gestão de turmas

O módulo utiliza as modalidades oficiais `NATACAO`, `TRIATHLON` e `BALLET`.
As migrations V1–V3 permanecem inalteradas; a V4 cria `class_group`,
`class_schedule` e `enrollment`, mantendo a tabela histórica `client`.

Rotas disponíveis em `/api/turmas`:

- `GET /api/turmas` — consulta paginada por nome, modalidade e status.
- `GET /api/turmas/{id}` — detalhes, horários e ocupação.
- `POST /api/turmas` e `PUT /api/turmas/{id}` — cadastro e edição.
- `PATCH /api/turmas/{id}/status` — ativação ou inativação lógica.
- `GET /api/turmas/{id}/matriculas` — histórico de matrículas.
- `POST /api/turmas/{id}/matriculas` — matrícula de aluno ativo.
- `DELETE /api/turmas/{id}/matriculas/{matriculaId}` — cancelamento lógico.
- `POST /api/turmas/{id}/matriculas/{matriculaId}/rematricula` — nova matrícula preservando o histórico.
- `GET /api/turmas/alunos/{alunoId}` — turmas e histórico do aluno.

As consultas exigem um usuário autenticado com um dos perfis existentes. Todas
as operações de escrita são exclusivas de `ADMIN`. A matrícula é serializada por
turma para impedir duas ocupações simultâneas da última vaga.
