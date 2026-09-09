# Postman - API Gestão de Patrimônio Imobiliário

Arquivos:

- `gestao-patrimonio-imobiliario.postman_collection.json` — todos os endpoints da API (schema Postman v2.1).
- `gestao-patrimonio-imobiliario.postman_environment.json` — environment local (`baseUrl = http://localhost:8080`).

## Como usar

1. No Postman: **Import** → selecione os dois arquivos.
2. Selecione o environment **"Gestão Patrimônio - Local"** (ou ajuste a variável `baseUrl` da collection).
3. Suba o backend (`./mvnw spring-boot:run`) com o PostgreSQL disponível em `localhost:5433`.

## Encadeamento de requests

As requisições de criação (POST) têm scripts de teste que salvam o `id` retornado nas variáveis
da collection. Ordem sugerida para um fluxo completo:

1. Usuários → **Criar usuário** (define `usuarioId`)
2. Imóveis → **Criar imóvel** (define `imovelId`)
3. Inquilinos → **Criar inquilino** (define `inquilinoId`)
4. Contratos → **Criar contrato** (define `contratoId`; gera os pagamentos de aluguel automaticamente)
5. Pagamentos de Aluguel → **Listar pagamentos por contrato** (define `pagamentoId` com o 1º da lista)
6. Pagamentos de Aluguel → **Registrar pagamento (baixa)** / **Registrar pagamento parcial**

## Endpoints cobertos

| Recurso | Base | Operações |
|---|---|---|
| Usuários | `/api/usuarios` | POST, GET (lista), GET `/{id}`, PUT `/{id}`, DELETE `/{id}` |
| Imóveis | `/api/imoveis` | POST, GET (lista), GET `/{id}`, PUT `/{id}`, DELETE `/{id}` |
| Inquilinos | `/api/inquilinos` | POST, GET (lista), GET `/{id}`, PUT `/{id}`, DELETE `/{id}` |
| Contratos | `/api/contratos` | POST, GET (lista), GET `/{id}`, PUT `/{id}`, DELETE `/{id}` |
| Pagamentos de Aluguel | `/api/pagamentos-aluguel` | POST, GET (lista, `?contratoId=`), GET `/{id}`, PUT `/{id}`, POST `/{id}/registrar-pagamento`, DELETE `/{id}` |
| Alertas | `/api/alertas` | GET (lista, `?usuarioId=`) |
| Notificações | `/api/notificacoes` | POST `/dispositivos`, DELETE `/dispositivos?token=`, POST `/enviar`, POST `/testar?token=` |
| Endereços | `/api/enderecos` | GET `/cep/{cep}` |
| Infra | — | GET `/actuator/health`, GET `/v3/api-docs` |

> Em desenvolvimento a API está liberada sem autenticação (`SecurityConfig` com `permitAll`), então nenhum token é necessário.
