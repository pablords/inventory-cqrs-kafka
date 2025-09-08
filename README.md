# inventory-cqrs-kafka

Este projeto é um sistema para gerenciar produtos e seus estoques, implementado com o padrão **CQRS (Command Query Responsibility Segregation)**. Ele é composto por dois serviços principais:

- **Command Service**: Responsável por gerenciar comandos que alteram o estado do sistema, como criar produtos e atualizar estoques. Utiliza PostgreSQL como banco de dados e publica eventos no Kafka.
- **Query Service**: Responsável por consultas e leitura de dados. Utiliza MongoDB como banco de dados e consome eventos do Kafka para manter um modelo de leitura atualizado.

---

## Arquitetura

O sistema segue o padrão **CQRS**, que separa as responsabilidades de escrita (Command) e leitura (Query) em serviços distintos. Isso permite maior escalabilidade, flexibilidade e desempenho, além de facilitar a implementação de modelos de leitura otimizados.

### Componentes Principais

- **Command Service**:
  - Gerencia operações de escrita no sistema.
  - Publica eventos no Kafka para notificar o Query Service sobre mudanças no estado.
  - Banco de dados: PostgreSQL.
  - Implementa o padrão **Outbox Pattern** para garantir a consistência entre o banco de dados e os eventos publicados no Kafka.
  - Utiliza **Kafka Connect** para sincronizar os dados do banco de escrita com o Kafka, garantindo que as mudanças sejam refletidas nos eventos publicados.

- **Query Service**:
  - Consome eventos do Kafka para atualizar o modelo de leitura.
  - Gerencia operações de leitura otimizadas.
  - Banco de dados: MongoDB.

- **Mensageria**:
  - Utiliza Apache Kafka para comunicação assíncrona entre os serviços.
  - Implementa **DLQ (Dead Letter Queue)** para lidar com mensagens que não podem ser processadas.

---

## Configuração

### Pré-requisitos

- Docker e Docker Compose instalados
- Java 17+
- Maven

### Subindo os serviços com Docker Compose

Para iniciar os serviços necessários (Kafka, Zookeeper, PostgreSQL e MongoDB), execute:

```bash
docker-compose up -d
```

---

## Endpoints

### Command Service

#### 1. Criar um Produto
**Endpoint:** `POST /api/v1/products`  
**Descrição:** Cria um novo produto com um nome e quantidade inicial.  

**Exemplo de comando `curl`:**
```bash
curl -X POST "http://localhost:8080/api/v1/products" \
     -H "Content-Type: application/json" \
     -d '{"name": "Produto1","initialQty":10}'
```

#### 2. Adicionar Estoque a um Produto
**Endpoint:** `POST /api/v1/products/{id}/add`  
**Descrição:** Adiciona uma quantidade ao estoque de um produto existente.  

**Exemplo de comando `curl`:**
```bash
curl -X POST "http://localhost:8080/api/v1/products/a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d/add" \
     -H "Content-Type: application/json" \
     -d '{"amount":5}'
```

#### 3. Subtrair Estoque de um Produto
**Endpoint:** `POST /api/v1/products/{id}/remove`  
**Descrição:** Subtrai uma quantidade ao estoque de um produto existente.  

**Exemplo de comando `curl`:**
```bash
curl -X POST "http://localhost:8080/api/v1/products/a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d/remove" \
     -H "Content-Type: application/json" \
     -d '{"amount":5}'
```
---

### Query Service

#### 1. Listar Todos os Produtos
**Endpoint:** `GET /api/v1/products`  
**Descrição:** Retorna todos os produtos do modelo de leitura.  

**Exemplo de comando `curl`:**
```bash
curl -X GET "http://localhost:8081/api/v1/products"
```

#### 2. Consultar Produto por ID
**Endpoint:** `GET /api/v1/products/{id}`  
**Descrição:** Retorna os detalhes de um produto específico.  

**Exemplo de comando `curl`:**
```bash
curl -X GET "http://localhost:8081/api/v1/products/a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d"
```


### 3. Criar idempotency order
**Endpoint:** `POST /api/v1/orders`  
**Descrição:** Cria um pedido.  

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 7b61a3c1-9a5b-4d7e-9e45-0ef9b6f2f0aa" \
  -d '{
    "items": [
      { "productId": "a1b2c3d4-e5f6-4a8b-9c0d-1e2f3a4b5c6d", "quantity": 2 },
      { "productId": "b2c3d4e5-f6a7-4b8c-9d0e-2f3a4b5c6d7e", "quantity": 5 }
    ],
    "customerId": "2f0e8d2b-8d7b-4db6-9a6f-1e3f9e9e3d01",
    "notes": "Entregar no horário comercial"
  }'

```
---

## Testando Concurrency

O script `simulate_concurrency.sh` pode ser usado para simular múltiplas atualizações concorrentes no estoque de um produto. Execute o script com:

```bash
bash simulate_concurrency.sh
```

---

## Logs e Debug

- **Command Service**: Porta `8080`
- **Query Service**: Porta `8081`
- **Kafka UI**: [http://localhost:9000](http://localhost:9000)

Certifique-se de que os serviços estão rodando corretamente antes de realizar os testes.

---

## Benefícios do CQRS

- **Separação de Responsabilidades**: Escrita e leitura são gerenciadas por serviços distintos, otimizados para suas respectivas operações.
- **Escalabilidade**: Os serviços podem ser escalados independentemente, dependendo da carga de leitura ou escrita.
- **Desempenho**: O modelo de leitura pode ser otimizado para consultas rápidas, enquanto o modelo de escrita foca na consistência.
- **Flexibilidade**: Facilita a implementação de diferentes modelos de leitura para atender a requisitos específicos.
- **Confiabilidade**: O uso do **Outbox Pattern**, **Kafka Connect** e da **DLQ** aumenta a resiliência e consistência do sistema.

---
