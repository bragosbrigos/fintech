# Fintech Platform - Documentação de Observabilidade

## Visão Geral

Esta plataforma financeira utiliza microsserviços com observabilidade completa para monitoramento, logging e alertas.

## Arquitetura de Observabilidade

### 1. Logging Centralizado

Cada microsserviço está configurado para:
- Gerar logs estruturados com trace ID e span ID
- Incluir informações de contexto (application name, thread, timestamp)
- Suportar diferentes níveis de log (DEBUG, INFO, WARN, ERROR)

**Formato do Log:**
```
2024-01-15 10:30:45 [http-nio-8080-exec-1] INFO  c.f.auth.service.AuthService - User authenticated successfully [traceId=abc123,spanId=def456]
```

### 2. Métricas com Spring Boot Actuator

Endpoints disponíveis em cada serviço:
- `/actuator/health` - Status de saúde da aplicação
- `/actuator/health/liveness` - Probe de liveness para Kubernetes
- `/actuator/health/readiness` - Probe de readiness para Kubernetes
- `/actuator/prometheus` - Métricas no formato Prometheus
- `/actuator/metrics` - Métricas detalhadas
- `/actuator/info` - Informações da aplicação

**Métricas Principais:**
- `http.server.requests` - Requisições HTTP (count, total time, max)
- `jvm.memory.used` - Uso de memória JVM
- `hikaricp.connections` - Pool de conexões do banco
- `kafka.consumer.records` - Records consumidos do Kafka

### 3. Distributed Tracing

Configurado com Micrometer Tracing:
- Sample rate: 100% (configurável)
- Propagação de contexto entre serviços
- Integration com OpenTelemetry

### 4. Alertas Configurados

#### Alertas Críticos
- **HighErrorRate**: Taxa de erro > 5% por 5 minutos
- **PodCrashLooping**: Pod reiniciou > 3 vezes em 1 hora
- **DatabaseConnectionPoolExhausted**: Pool de conexões > 90%

#### Alertas de Warning
- **HighResponseTime**: P95 > 2 segundos
- **LowDiskSpace**: Disco < 10% disponível
- **HighMemoryUsage**: Memória > 85% utilizada
- **KafkaConsumerLag**: Lag > 1000 mensagens

## Configuração por Serviço

### Auth Service
- Porta: 8081
- Endpoints de saúde: `/actuator/health`
- Logs: `com.fintech.auth=DEBUG`

### User Service
- Porta: 8082
- Endpoints de saúde: `/actuator/health`
- Logs: `com.fintech.user=DEBUG`

### Wallet Service
- Porta: 8083
- Endpoints de saúde: `/actuator/health`
- Logs: `com.fintech.wallet=DEBUG`

### Transaction Service
- Porta: 8084
- Endpoints de saúde: `/actuator/health`
- Logs: `com.fintech.transaction=DEBUG`

### Notification Service
- Porta: 8085
- Endpoints de saúde: `/actuator/health`
- Logs: `com.fintech.notification=DEBUG`

### Admin Service
- Porta: 8086
- Endpoints de saúde: `/actuator/health`
- Logs: `com.fintech.admin=DEBUG`

### API Gateway
- Porta: 8080
- Endpoints de saúde: `/actuator/health`
- Logs: `com.fintech.gateway=DEBUG`

## Dashboard Recomendado (Grafana)

### Painéis Sugeridos

1. **Visão Geral do Sistema**
   - Request rate por serviço
   - Error rate por serviço
   - Latência média e P95
   - Status dos pods

2. **Performance de Serviços**
   - Tempo de resposta por endpoint
   - Throughput por serviço
   - Taxa de erro por tipo de HTTP status

3. **Recursos**
   - Uso de CPU por pod
   - Uso de memória por pod
   - Uso de disco

4. **Banco de Dados**
   - Conexões ativas no pool
   - Tempo médio de query
   - Transações por segundo

5. **Kafka**
   - Consumer lag por tópico
   - Messages produced/consumed
   - Partição health

## Acesso aos Endpoints

### Localmente
```bash
# Auth Service
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/prometheus

# User Service
curl http://localhost:8082/actuator/health

# Wallet Service
curl http://localhost:8083/actuator/health

# Transaction Service
curl http://localhost:8084/actuator/health

# Notification Service
curl http://localhost:8085/actuator/health

# Admin Service
curl http://localhost:8086/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health
```

### No Kubernetes
```bash
# Port forwarding para um serviço
kubectl port-forward svc/auth-service 8081:80 -n fintech-staging

# Acessar métricas via Prometheus
kubectl port-forward svc/prometheus-k8s 9090:9090 -n monitoring
```

## Integração com Ferramentas

### Prometheus
Scrape config automática via ServiceMonitor

### Grafana
Dashboards importáveis via ConfigMap

### Jaeger/Tempo
Distributed tracing visual

### Loki
Logs centralizados

### Alertmanager
Gerenciamento de alertas e notificações

## Troubleshooting

### Logs não aparecem
1. Verificar nível de log nas configurações
2. Confirmar se o pod está rodando
3. Checar se há espaço em disco

### Métricas não são coletadas
1. Verificar se Actuator está exposto
2. Confirmar ServiceMonitor no Kubernetes
3. Checar network policies

### Alertas não disparam
1. Validar regras no Prometheus
2. Verificar integração com Alertmanager
3. Checar thresholds das regras

## Melhores Práticas

1. **Logging**
   - Usar logs estruturados (JSON em produção)
   - Incluir correlation IDs
   - Evitar logs sensíveis (PII, senhas)

2. **Métricas**
   - Definir SLIs e SLOs claros
   - Usar histogramas para latência
   - Monitorar business metrics

3. **Alertas**
   - Alertar sobre sintomas, não causas
   - Evitar alert fatigue
   - Ter runbooks para cada alerta

4. **Tracing**
   - Manter sample rate adequado
   - Propagar contexto em todas as chamadas
   - Instrumentar chamadas externas
