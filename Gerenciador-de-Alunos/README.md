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
```

Em seguida, execute a aplicação:

```powershell
mvn spring-boot:run
```

### Docker Compose

Crie um arquivo `.env` local a partir do exemplo e preencha `JWT_SECRET`, ou
exporte a variável no terminal antes de subir os serviços:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
docker compose -f src/main/resources/docker-compose.yml up --build
```

O serviço `api` recebe a variável diretamente do ambiente. O Compose falha de
forma explícita se `JWT_SECRET` não estiver definida.

## Desenvolvimento

- Java 21
- MySQL 8 para desenvolvimento
- H2 em memória para testes

Execute os testes com `mvn test`.
