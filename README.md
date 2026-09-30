# Microservices — ATITUS

| Serviço      | Porta | Banco        | Config (Consul)          |
|--------------|-------|--------------|--------------------------|
| product-api  | 8000  | db_product   | `config/product-api/`    |
| currency-api | 8100  | db_currency  | `config/currency-api/`   |

## Execução
1. PostgreSQL rodando com os bancos `db_product` e `db_currency` criados.
2. `start_consul.bat` — sobe o Consul (dev) e cadastra as configurações.
3. Em cada serviço: `mvnw spring-boot:run` (Flyway aplica as migrations).

## Teste
```
GET http://localhost:8100/currencies?source=USD&target=BRL
```
```json
{"sourceCurrency":"USD","targetCurrency":"BRL","conversionRate":5.15,"environment":"Currency API running in Port: 8100"}
```
