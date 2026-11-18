# SAI – Serviço de Agenda Inteligente (backend)

Spring Boot 4 · Java 17 · PostgreSQL 16 · Flyway · JWT.

## Como rodar
1. `cp .env.example .env` e ajuste os valores
2. `docker compose up -d`
3. `./mvnw spring-boot:run` (as migrations V1–V7 rodam sozinhas)

## Endpoints
**Públicos (sem token)**
| Método | Rota | O que faz |
|---|---|---|
| POST | /auth/cadastro | cadastra profissional (`nome`, `email`, `senha`, `telefone?`) |
| POST | /auth/login | devolve o token JWT |
| GET | /publico/{link} | nome e procedimentos do(a) profissional |
| GET | /publico/{link}/horarios?procedimentoId=&data=AAAA-MM-DD | horários livres |
| POST | /publico/{link}/agendamentos | cliente agenda; devolve `token` do link único |
| GET/PUT/DELETE | /publico/agendamentos/{token} | consulta / remarca / cancela |

**Profissional (Authorization: Bearer ...)**
| Método | Rota |
|---|---|
| GET/PUT | /perfil (mostra `linkPublico`) |
| GET/POST/PUT/DELETE | /procedimentos |
| GET/POST/DELETE | /disponibilidades |
| GET/POST/DELETE | /bloqueios |
| GET/POST/PUT/DELETE | /agendamentos |

## Regras
30 min de antecedência (RF050) · janela de 30 dias (RF051) · sem sobreposição, garantida pelo banco (RF047) · cancelar libera o horário (RF049) · cliente cancela/remarca com no mínimo `app.agenda.horas-antecedencia-cliente` h (RF052) · auditoria de quem criou/remarcou/cancelou (RF045).

## WhatsApp
Não integrado. `NotificadorAgendamento` é o encaixe: crie um `@Component` que o implemente e remova `LogNotificador`.
