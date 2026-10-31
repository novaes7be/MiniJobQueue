# MiniJobQueue

Fila de tarefas assíncronas com **Java 21**, **Spring Boot** e **Redis**, criada para praticar **Docker**.

Uma API recebe jobs, coloca numa fila no Redis e um worker consome e atualiza o status de cada um.

## Como funciona

```
cliente --POST /jobs--> API --LPUSH--> Redis (lista jobs:queue)
                                          |
                                       BRPOP
                                          v
                                       Worker --> atualiza hash job:{id}
cliente --GET /jobs/{id}--> API --HGETALL--> Redis
```

- **Fila:** `LIST` do Redis (`jobs:queue`), FIFO (entra pela esquerda, sai pela direita)
- **Estado do job:** `HASH` do Redis (`job:{id}`) com os campos `payload` e `status`
- **Status:** `PENDING` -> `PROCESSING` -> `DONE` (ou `FAILED`)
- **Worker:** thread iniciada por um `ApplicationRunner`, que usa `rightPop` bloqueante com timeout de 5s

## Stack

- Java 21
- Spring Boot 3 (Web, Data Redis)
- Redis 7
- Docker

## Endpoints

| Método | Rota         | Descrição                          | Resposta                |
|--------|--------------|------------------------------------|-------------------------|
| POST   | `/jobs`      | Cria um job (payload no body, texto) | `200` com o job criado  |
| GET    | `/jobs/{id}` | Consulta o job                     | `200` com o job ou `404`|

Exemplo:

```bash
curl -X POST localhost:8080/jobs -H "Content-Type: text/plain" -d "meu job"
curl localhost:8080/jobs/<id>
```

## Como rodar (local)

1. Suba o Redis:

```bash
docker run -d --name redis -p 6379:6379 redis:7
```

2. Rode a aplicação:

```bash
./mvnw spring-boot:run
```

A configuração do Redis fica em `application.properties` (`spring.data.redis.host` e `spring.data.redis.port`).

## Estrutura

```
src/main/java/com/contatodireto/minijobqueue/jobs/
  api/JobController.java
  model/Job.java
  queue/JobQueue.java
  status/JobStatus.java
  worker/JobWorker.java
```

## Roadmap

- [x] Modelo `Job` e `JobQueue` (enqueue, take, updateStatus, find)
- [x] API REST (`POST /jobs`, `GET /jobs/{id}`)
- [x] Worker consumindo a fila
- [ ] `Dockerfile` multi-stage
- [ ] `docker-compose.yml` com `api`, `worker` e `redis`
- [ ] Escalar workers (`docker compose up --scale worker=3`)
- [ ] Rede customizada e volume para o Redis
- [ ] `healthcheck` e `depends_on`
- [ ] Profiles do Spring (`api` / `worker`) via variável de ambiente
- [ ] Dead letter queue para jobs com falha
- [ ] Redis Insight para visualizar a fila
