# E-commerce Application

Este é um projeto de uma aplicação de e-commerce desenvolvida com Spring Boot, utilizando MySQL como banco de dados relacional e Redis como cache. O projeto é containerizado com Docker e gerenciado via Docker Compose.

## Pré-requisitos

Antes de rodar o projeto, certifique-se de ter os seguintes itens instalados em sua máquina:

- **Docker**: Versão 20.10 ou superior. [Instale o Docker](https://docs.docker.com/get-docker/).
- **Docker Compose**: Versão 1.27.0 ou superior (geralmente incluído com o Docker Desktop). [Instale o Docker Compose](https://docs.docker.com/compose/install/).
- **Git**: Para clonar o repositório. [Instale o Git](https://git-scm.com/downloads).

Opcional:
- **Java 17**: Necessário apenas se você quiser rodar a aplicação fora do Docker ou compilar manualmente. [Instale o Java](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html).
- **Maven**: Para compilar o projeto localmente, caso necessário. [Instale o Maven](https://maven.apache.org/install.html).

## Estrutura do Projeto

- **app**: Aplicação Spring Boot que implementa a lógica de negócios do e-commerce.
- **mysql**: Banco de dados MySQL para persistência de dados.
- **redis**: Cache Redis para otimização de desempenho.

## Como Rodar o Projeto

Siga os passos abaixo para rodar a aplicação localmente usando Docker Compose.

### 1. Clone o Repositório

Clone o projeto para sua máquina:

```bash
  git clone https://github.com/lucashenriquemtos/case-tecnico
cd ecommerce-app
```

### 2. Subir os Serviços com Docker Compose

```bash
  docker-compose up -d
```

## Endpoints da API

A aplicação expõe uma API RESTful para gerenciamento de produtos, pedidos, autenticação e relatórios. Todos os endpoints estão protegidos por autenticação baseada em JWT (JSON Web Token), exceto o endpoint de login. Abaixo estão os detalhes de uso, incluindo métodos, URLs, permissões necessárias e exemplos.

### Autenticação

Antes de acessar os endpoints protegidos, é necessário obter um token JWT via login.

#### POST `/auth/login`
Realiza a autenticação do usuário e retorna um token JWT.

- **Permissões**: Público (não requer autenticação).
- **Corpo da Requisição**:
  ```json
  {
    "email": "user@ecommerce.com",
    "senha": "123456"
  }
- **Resposta**
    ```json
    {
      "token": "jwt_token_aqui"
    }
    ```

### Produtos

#### POST `/produtos`

Adiciona um novo produto ao catálogo.

- **Permissões**: ADMIN (necessário estar autenticado com papel de administrador).
- **Corpo da Requisição**:
  ```json
    {
      "nome": "Cadeira Gamer",
      "descricao": "Cadeira ergonômica para gamers com apoio para os braços ajustável",
      "preco": 1299.99,
      "categoria": "Móveis",
      "quantidadeEstoque": 15
    }
    ```
- **Resposta**
    ```json
    {
      "id": "0de96889-2165-457c-a0c6-e71b624afaea",
      "nome": "Cadeira Gamer",
      "descricao": "Cadeira ergonômica para gamers com apoio para os braços ajustável",
      "preco": 1299.99,
      "categoria": "Móveis",
      "quantidadeEstoque": 15,
      "dataCriacao": "2025-03-19T14:20:13.597927502",
      "dataAtualizacao": null
    }
    ```
#### GET `/produtos`
Busca um produto pelo ID.

- **Permissões**: ADMIN, USER (necessário estar autenticado com qualquer um dos papéis).
- **Parâmetros**: 
  - `id` (UUID): O ID do produto a ser buscado


- **Resposta**
    ```json
    {
      "id": "uuid_do_produto",
      "nome": "Produto Exemplo",
      "descricao": "Descrição do produto",
      "preco": 100.00
    }
    ```
#### PUT `/produtos`
Atualiza as informações de um produto existente.

- **Permissões**: ADMIN (necessário estar autenticado com papel de administrador).
- **Parâmetros**:
    - `id` (UUID): O ID do produto a ser atualizado
  
- **Corpo da Requisição**:
  ```json
  {
    "nome": "Produto Atualizado",
    "descricao": "Nova descrição do produto",
    "preco": 120.00
  }
  ```
- **Resposta**
    ```json
    {
      "id": "uuid_do_produto",
      "nome": "Produto Atualizado",
      "descricao": "Nova descrição do produto",
      "preco": 120.00
    }
    ```

#### DELETE `/produtos`
Remove um produto do catálogo.

- **Permissões**: ADMIN (necessário estar autenticado com papel de administrador).
- **Parâmetros**:
    - `id` (UUID): O ID do produto a ser removido

- **Resposta**
    - `Status`: 204 No Content (se a remoção for bem-sucedida)

### Pedidos

#### POST `/pedidos`

Cria um novo pedido para o usuário autenticado.

- **Permissões**: ADMIN, USER (necessário estar autenticado com qualquer um dos papéis)
- **Corpo da Requisição**:
  ```json
  [
    {
      "produtoId": "uuid_do_produto",
      "quantidade": 2
    },
    {
      "produtoId": "uuid_outro_produto",
      "quantidade": 1
    }
  ]

- **Resposta**
```json
      {
        "id": "uuid_do_pedido",
        "usuarioId": "id_do_usuario",
        "itens": [
                    {
                    "produtoId": "uuid_do_produto",
                    "quantidade": 2,
                    "precoUnitario": 50.00
                    }
      ],
      "status": "PENDENTE"
      }
```

#### POST `/pedidos/{id}/pagar`
Realiza o pagamento de um pedido.

- **Permissões**: ADMIN, USER (necessário estar autenticado com qualquer um dos papéis)
- **Corpo da Requisição**:
  - `id` (UUID): O ID do pedido a ser pago.
- **Resposta**:
    ```json
    {
      "id": "uuid_do_pedido",
      "status": "PAGO",
      "itens": [
        {
          "produtoId": "uuid_do_produto",
          "quantidade": 2,
          "precoUnitario": 50.00
        }
      ]
    }
  ```

#### GET `/pedidos/meus-pedidos`
Lista todos os pedidos do usuário autenticado.

- **Permissões**: ADMIN, USER (necessário estar autenticado com qualquer um dos papéis)
  - **Resposta**:
      ```json
    [
        {
          "id": "uuid_do_pedido",
          "usuarioId": "id_do_usuario",
          "itens": [
            {
              "produtoId": "uuid_do_produto",
              "quantidade": 2,
              "precoUnitario": 50.00
            }
          ],
          "status": "PENDENTE"
        }
    ]

### Relatórios

#### `GET /relatorio/top-usuarios`

Retorna os 5 usuários com maior número de pedidos.

- **Permissões**: ADMIN (necessário estar autenticado com papel de administrador)
- **Resposta**:
  ```json
  [
    {
      "usuarioId": "id_do_usuario",
      "email": "usuario@ecommer.com",
      "totalGasto": 1000.00
    },
    {
      "usuarioId": "id_outro_usuario",
      "email": "usuario@ecommer.com",
      "totalGasto": 179.00
    }
  ]

#### `GET /relatorio/ticket-medio-por-usuario`

Retorna o ticket médio de cada usuário, ou seja, o valor médio dos pedidos feitos por cada usuário.

- **Permissões**: ADMIN (necessário estar autenticado com papel de administrador)
- **Resposta**:
  ```json
  [
    {
      "usuarioId": "id_do_usuario",
      "email": "usuario@ecommer.com",
      "ticketMedio": 150.00
    },
    {
      "usuarioId": "id_outro_usuario",
      "email": "usuario@ecommer.com",
      "ticketMedio": 120.00
    }
  ]

#### `GET /relatorio/faturamento-mensal`

Retorna o faturamento mensal de um ano e mês específicos.

- **Permissões**: ADMIN (necessário estar autenticado com papel de administrador)
- **Parâmetros**:
    - `ano` (Integer): O ano do faturamento a ser calculado (ex: 2025).
    - `mes` (Integer): O mês do faturamento a ser calculado (ex: 3 para março).

- **Resposta**:
  ```json
  {
    "ano": 2025,
    "mes": 3,
    "faturamento": 5000.00
  }
