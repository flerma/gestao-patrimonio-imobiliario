# Notificações push dos alertas

Envia por push (Expo) um **resumo diário dos alertas do dashboard** para os aparelhos
que ativaram as notificações no app.

## Como funciona

```
app (Ajustes → "Ativar notificações")
  └─ pede permissão + pega o Expo push token
  └─ POST /api/notificacoes/dispositivos { expoPushToken, usuarioId, plataforma }

backend
  ├─ @Scheduled diário 08:00 (America/Sao_Paulo)  →  NotificacaoService.enviarResumoDeAlertas()
  │     para cada proprietário com aparelho registrado:
  │       AlertaService.listarParaUsuario(usuarioId)   (mesmas regras do dashboard)
  │       se houver alertas → 1 push por aparelho ("3 alertas (2 críticos): Contrato vencido — …")
  │       tokens recusados (DeviceNotRegistered) são desativados
  └─ exp.host/--/api/v2/push/send
```

O agendador **só dispara com a aplicação no ar**. Para disparar na hora:
`POST /api/notificacoes/enviar`.

## Endpoints

| Método | Rota | Uso |
|---|---|---|
| `GET`  | `/api/alertas?usuarioId=` | lista os alertas (reavaliados do banco) |
| `POST` | `/api/notificacoes/dispositivos` | registra/atualiza o token do aparelho |
| `DELETE` | `/api/notificacoes/dispositivos?token=` | remove o token |
| `POST` | `/api/notificacoes/enviar` | dispara o resumo agora (todos os aparelhos) |
| `POST` | `/api/notificacoes/testar?token=` | push fixo de teste para um token |

## Configuração

`application.yml` / variáveis de ambiente:

| Chave | Padrão | Observação |
|---|---|---|
| `integracao.expo.push-url` | `https://exp.host/--/api/v2/push/send` | — |
| `integracao.expo.access-token` (`EXPO_ACCESS_TOKEN`) | vazio | só se ativar *Enhanced Security for Push* no projeto Expo |
| `notificacao.resumo-diario.cron` | `0 0 8 * * *` | horário do resumo (zona America/Sao_Paulo) |

Tabela nova: `dispositivos_push` (Liquibase `007-create-table-dispositivos-push.sql`).

## Pré-requisitos no app (mobile)

O token de push da Expo exige:

1. **EAS project id** — rode uma vez `npx eas init` na pasta `gestao-patrimonio-mobile`
   (popula `expo.extra.eas.projectId` em `app.json`).
2. **Build de desenvolvimento** — `npx expo run:android` / `run:ios` ou um build EAS.
   Push remoto **não funciona no Expo Go** (Android, desde o SDK 53).

Sem esses dois, o app continua funcionando normalmente; a tela de Ajustes apenas
avisa que as notificações não puderam ser ativadas.
