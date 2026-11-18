# SAI – Serviço de Agenda Inteligente (backend)

Spring Boot 4 · Java 17 · PostgreSQL 16 · Flyway · JWT.

## Como rodar
1. `cp .env.example .env` e ajuste os valores
2. `docker compose up -d`
3. `./mvnw spring-boot:run` (as migrations V1–V8 rodam sozinhas)

## Tipos de conta e login
Há três tipos de conta, cada um com seu login. O token JWT traz o tipo e cada rota só aceita o tipo certo.
Enviar sempre `Authorization: Bearer <token>` nas rotas protegidas.

| Conta | Cadastro | Login |
|---|---|---|
| Profissional | `POST /auth/cadastro` (`nome`, `email`, `senha`, `telefone?`) | `POST /auth/login` |
| Empresa | `POST /auth/empresa/cadastro` | `POST /auth/empresa/login` |
| Cliente (opcional) | `POST /auth/cliente/cadastro` (telefone obrigatório) | `POST /auth/cliente/login` |

Redefinir senha da cliente (RF025): `POST /auth/cliente/senha/codigo` (`email`, `canal` EMAIL|SMS) e
`POST /auth/cliente/senha/redefinir` (`email`, `codigo`, `novaSenha`). O código é de 6 dígitos, vale 15 min e, por enquanto, aparece no log da aplicação.

## Cliente (sem login) – `/publico`
| Método | Rota | O que faz |
|---|---|---|
| GET | /publico/{link} | `tipo` (PROFISSIONAL ou EMPRESA) e procedimentos, cada um com os profissionais que o realizam |
| GET | /publico/{link}/horarios?procedimentoId=&data=AAAA-MM-DD | horários livres de um profissional |
| GET | /publico/{link}/horarios?chave=&data=AAAA-MM-DD | "qualquer profissional disponível" (link de empresa) |
| POST | /publico/{link}/agendamentos | agenda (`procedimentoId` OU `chave`, `nomeCliente`, `telefoneCliente`, `inicio`); logada, usa os dados da conta |
| GET/PUT/DELETE | /publico/agendamentos/{token} | consulta / remarca (`inicio`) / cancela pelo link único |

## Profissional (token PROFISSIONAL)
`/perfil` (GET/PUT; `linkPublico`, `antecedenciaLembreteMinutos`, `prazoCancelamentoHoras`) · `/procedimentos` · `/disponibilidades` (DELETE aceita `?confirmar=true`) ·
`/bloqueios` (POST aceita `cancelarAgendamentos: true`) · `/agendamentos` (GET `?de=&ate=`, POST manual, PUT remarcar, DELETE cancelar) ·
`/vinculos/convites` (GET), `/vinculos/convites/{id}/aceitar|recusar` (POST), `/vinculos/atual` (GET), `/vinculos/sair` (POST).

## Empresa (token EMPRESA)
`GET /empresa` · `POST /empresa/link/regenerar` · `PUT /empresa/link` (`ativo`) · `POST /empresa/convites` (`emailProfissional`) · `GET /empresa/vinculos` ·
`GET /empresa/procedimentos` · `PUT|DELETE /empresa/procedimentos/{id}` (libera/tira do link; `nomeNoLink` agrupa duplicados) ·
`GET /empresa/profissionais/{id}/agendamentos` (somente leitura) · `GET|POST /empresa/agendamentos` · `PUT|DELETE /empresa/agendamentos/{id}`.

## Cliente com conta (token CLIENTE)
`GET /cliente/agendamentos` (cada item traz o `token` para remarcar/cancelar).

## Regras
30 min de antecedência (RF050) · janela de 30 dias (RF051) · sem sobreposição, garantida pelo banco (RF047) · cancelar libera o horário (RF049) ·
cliente e empresa só cancelam/remarcam respeitando `prazoCancelamentoHoras` do(a) profissional (RF052) · auditoria de origem/remarcação/cancelamento (RF045) ·
sair da empresa desativa as liberações no link, sem apagar nada (RF053) · remover procedimento liberado em empresa é bloqueado (RF004).

## Erros
JSON `{ "status", "mensagem", ... }`: 400 validação, 401 login, 403 tipo de conta errado, 404 não encontrado, 409 conflito (horário ocupado, prazo, etc.).

## Fora desta versão
WhatsApp (RF038, RF040–RF043): `NotificadorAgendamento` e `EnviadorCodigo` são os encaixes (hoje só escrevem no log).
Foto/biografia do profissional no link (RF030) não foram implementadas.
