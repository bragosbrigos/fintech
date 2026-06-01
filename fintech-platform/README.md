# Fintech Platform - Microservices Architecture

## 🏦 Visão Geral

Plataforma financeira moderna que simula uma fintech, construída com arquitetura de microsserviços, escalável e resiliente.

## 🚀 Tecnologias Utilizadas

- **Backend**: Java 21, Spring Boot 3.x, Spring Security, JWT
- **Banco de Dados**: PostgreSQL 15
- **Cache**: Redis 7
- **Mensageria**: Apache Kafka
- **Containerização**: Docker & Docker Compose
- **CI/CD**: GitHub Actions
- **Testes**: JUnit 5, Testcontainers, Mockito
- **Documentação**: OpenAPI/Swagger
- **Observabilidade**: Logging centralizado, métricas e alertas

## 🏗️ Arquitetura de Microsserviços

```
┌─────────────────┐
│   API Gateway   │ (Port: 8080)
└────────┬────────┘
         │
    ┌────┴────┬───────────┬──────────────┬──────────────────┐
    │         │           │              │                  │
┌───▼───┐ ┌──▼────┐ ┌───▼──────┐ ┌─────▼──────┐ ┌────────▼────────┐
│ Auth  │ │ User  │ │  Wallet  │ │Transaction │ │  Notification   │
│Service│ │Service│ │ Service  │ │  Service   │ │    Service      │
│:8081  │ │:8082  │ │  :8083   │ │   :8084    │ │     :8085       │
└───┬───┘ └───┬───┘ └────┬─────┘ └─────┬──────┘ └────────┬────────┘
    │         │          │             │                  │
    └─────────┴──────────┴─────────────┴──────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │   Infrastructure  │
                    ├───────────────────┤
                    │  PostgreSQL :5432 │
                    │  Redis    :6379   │
                    │  Kafka    :9092   │
                    └───────────────────┘
```

## 📋 Serviços

### 1. Auth Service (Porta: 8081)
- Cadastro de usuários
- Login e logout
- Geração e validação de JWT
- Controle de permissões (roles: USER, ADMIN)

### 2. User Service (Porta: 8082)
- Gestão de perfis de usuário
- Atualização de dados pessoais
- Histórico de login
- Validação de usuários

### 3. Wallet Service (Porta: 8083)
- Criação de carteiras digitais
- Gestão de saldo (depósitos e retiradas)
- Bloqueio/desbloqueio de saldo
- Relatórios de saldo

### 4. Transaction Service (Porta: 8084)
- Transferências entre carteiras
- Pagamentos simulados (boleto, QR code, PIX)
- Registro de transações com timestamp
- Regras de idempotência
- Validação de transações

### 5. Notification Service (Porta: 8085)
- Envio de e-mails simulados
- Push notifications
- Alertas de transações
- Relatórios periódicos

### 6. API Gateway (Porta: 8080)
- Roteamento de requisições
- Autenticação centralizada
- Rate limiting
- Logging de requests

## 🛠️ Como Executar

### Pré-requisitos
- Docker e Docker Compose instalados
- Java 21 (para desenvolvimento local)
- Maven 3.9+

### Subir toda a infraestrutura

```bash
cd fintech-platform
docker-compose up -d
```

### Verificar status dos serviços

```bash
docker-compose ps
```

### Acessar logs

```bash
docker-compose logs -f [service-name]
```

### Parar serviços

```bash
docker-compose down
```

## 📡 Endpoints da API

### Auth Service
```
POST /api/auth/register          - Registrar novo usuário
POST /api/auth/login             - Login
POST /api/auth/logout            - Logout
POST /api/auth/refresh-token     - Refresh token
GET  /api/auth/me                - Dados do usuário autenticado
```

### User Service
```
GET    /api/users/{id}           - Buscar usuário por ID
PUT    /api/users/{id}           - Atualizar usuário
GET    /api/users/{id}/logins    - Histórico de logins
```

### Wallet Service
```
POST   /api/wallets              - Criar carteira
GET    /api/wallets/{userId}     - Buscar carteira por usuário
GET    /api/wallets/{id}/balance - Saldo da carteira
POST   /api/wallets/{id}/deposit - Depósito
POST   /api/wallets/{id}/withdraw- Retirada
PUT    /api/wallets/{id}/lock    - Bloquear carteira
PUT    /api/wallets/{id}/unlock  - Desbloquear carteira
```

### Transaction Service
```
POST   /api/transactions         - Criar transação
GET    /api/transactions/{id}    - Buscar transação por ID
GET    /api/transactions/user/{userId} - Histórico de transações
GET    /api/transactions/wallet/{walletId} - Transações por carteira
```

### Notification Service
```
GET    /api/notifications/{userId} - Notificações do usuário
PUT    /api/notifications/{id}/read - Marcar como lida
GET    /api/notifications/unread/{userId} - Não lidas
```

## 🧪 Testes

### Executar testes unitários

```bash
./mvnw test
```

### Executar testes de integração com Testcontainers

```bash
./mvnw verify -Pintegration
```

## 📊 Dashboard Administrativo

O dashboard administrativo está disponível em `/admin` (apenas para usuários com role ADMIN) e inclui:

- Métricas de transações
- Volume financeiro por período
- Número de usuários ativos
- Gráficos de crescimento
- Logs de auditoria

## 🔒 Segurança

- JWT para autenticação stateless
- Senhas criptografadas com BCrypt
- HTTPS em produção
- Rate limiting por IP
- Validação de input
- Proteção contra CSRF e XSS

## 📈 Observabilidade

### Logs
- Centralizados via ELK Stack (opcional)
- Correlation IDs para tracing distribuído

### Métricas
- Spring Boot Actuator
- Prometheus + Grafana (opcional)

### Health Checks
```bash
GET /actuator/health
GET /actuator/metrics
GET /actuator/info
```

## 🔄 Fluxo de Transação

```
1. Usuário solicita transferência
2. API Gateway valida JWT
3. Transaction Service recebe requisição
4. Valida saldo no Wallet Service (via Feign Client)
5. Publica evento "TransactionCreated" no Kafka
6. Wallet Service consome evento e debita saldo
7. Wallet Service publica "BalanceUpdated"
8. Notification Service consome e envia notificação
9. Transaction Service atualiza status da transação
```

## 🚀 CI/CD

O pipeline de CI/CD está configurado no `.github/workflows/` e executa:

1. Build e testes automatizados
2. Análise de código com SonarQube
3. Build de imagens Docker
4. Push para registry
5. Deploy em Kubernetes (opcional)

## 📝 Documentação API

Cada serviço possui documentação Swagger disponível em:
```
http://localhost:{port}/swagger-ui.html
```

Exemplo: `http://localhost:8081/swagger-ui.html` para Auth Service

## 👥 Roles e Permissões

| Role | Permissões |
|------|-----------|
| USER | Operações básicas na própria conta |
| ADMIN | Acesso total, dashboard administrativo |

## 🎯 Próximos Passos

- [ ] Implementar Kubernetes manifests
- [ ] Configurar Istio para service mesh
- [ ] Adicionar circuit breaker com Resilience4j
- [ ] Implementar saga pattern para transações distribuídas
- [ ] Configurar monitoring com Prometheus/Grafana
- [ ] Adicionar suporte a PIX real

## 📄 Licença

MIT License
