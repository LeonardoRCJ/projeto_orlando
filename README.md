# Projeto Orlando API

API REST para gerenciamento de devedores, contas, dívidas, pagamentos e relatórios financeiros.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Executando a Aplicação](#executando-a-aplicação)
- [Documentação da API](#documentação-da-api)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Autenticação](#autenticação)
- [Endpoints Principais](#endpoints-principais)
- [Modelo de Dados](#modelo-de-dados)
- [Testes](#testes)
- [Deploy](#deploy)

## 🎯 Sobre o Projeto

O **Projeto Orlando** é uma API REST desenvolvida em Spring Boot para gerenciamento financeiro de empresas que trabalham com devedores. A aplicação permite:

- Gerenciar empresas e usuários
- Cadastrar e gerenciar devedores
- Criar contratos entre empresas e devedores
- Registrar dívidas e pagamentos
- Gerar relatórios financeiros diversos
- Gerenciar notificações

### Funcionalidades Principais

- ✅ Autenticação JWT
- ✅ Multi-tenancy (cada empresa vê apenas seus dados)
- ✅ Gerenciamento completo de devedores e contas
- ✅ Contratos com status (RASCUNHO, ATIVO, CONCLUIDO, CANCELADO)
- ✅ Cálculo automático de saldo das contas
- ✅ Relatórios automáticos e manuais
- ✅ Auditoria financeira
- ✅ Cascade deletion (deletar empresa/devedor remove dados relacionados)
- ✅ Documentação Swagger/OpenAPI completa

## 🛠 Tecnologias

- **Java 21**
- **Spring Boot 4.0.0**
- **Spring Data JPA**
- **Spring Security**
- **H2 Database** (desenvolvimento)
- **JWT (Auth0)**
- **Lombok**
- **Maven**
- **Swagger/OpenAPI 3** (SpringDoc)
- **JUnit 5** (testes)

## 📦 Pré-requisitos

- Java 21 ou superior
- Maven 3.6+ ou superior
- IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)

## 🚀 Instalação

1. Clone o repositório:
```bash
git clone <url-do-repositorio>
cd projeto_orlando
```

2. Compile o projeto:
```bash
mvn clean install
```

## ⚙️ Configuração

### Arquivo `application.properties`

O arquivo de configuração principal está em `src/main/resources/application.properties`:

```properties
# Application Configuration
spring.application.name=projeto-orlando
server.port=8080

# JWT Configuration
jwt.secret=your-secret-key-change-this-in-production-use-a-strong-random-key
jwt.expiration=3600000

# Database Configuration (H2 - Development)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Configuração de Produção

Para produção, altere:

1. **Banco de dados**: Configure um banco PostgreSQL, MySQL ou outro suportado
2. **JWT Secret**: Use uma chave forte e aleatória
3. **H2 Console**: Desabilite (`spring.h2.console.enabled=false`)

## ▶️ Executando a Aplicação

### Via Maven:
```bash
mvn spring-boot:run
```

### Via IDE:
Execute a classe `ProjetoOrlandoApplication.java`

### Via JAR:
```bash
mvn clean package
java -jar target/projeto-orlando-1.0.0.jar
```

A aplicação estará disponível em: `http://localhost:8080`

## 📚 Documentação da API

### Swagger UI

Acesse a documentação interativa da API em:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Endpoints Públicos

- `POST /api/users/register` - Registrar novo usuário
- `POST /api/auth/login` - Fazer login e obter token JWT

### Endpoints Protegidos (requerem autenticação JWT)

Todos os outros endpoints requerem o header:
```
Authorization: Bearer <token>
```

## 🏗 Estrutura do Projeto

```
projeto_orlando/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tech/devleo/projeto_orlando/
│   │   │       ├── config/          # Configurações (Security, JWT, OpenAPI)
│   │   │       ├── controller/     # Controllers REST
│   │   │       ├── domain/          # Entidades JPA
│   │   │       ├── dto/            # Data Transfer Objects
│   │   │       ├── exception/      # Tratamento de exceções
│   │   │       ├── repository/     # Repositórios JPA
│   │   │       └── service/        # Lógica de negócio
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/                   # Testes unitários e de integração
│       └── resources/
│           └── application-test.properties
├── pom.xml
└── README.md
```

## 🔐 Autenticação

### 1. Registrar Usuário

```http
POST /api/users/register
Content-Type: application/json

{
  "username": "usuario123",
  "email": "usuario@example.com",
  "password": "senha123"
}
```

### 2. Fazer Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "usuario@example.com",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Usar o Token

Inclua o token em todas as requisições protegidas:

```http
GET /api/devedores
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 📡 Endpoints Principais

### Empresas

- `GET /api/empresas/me` - Obter minha empresa
- `POST /api/empresas` - Criar empresa
- `PUT /api/empresas` - Atualizar empresa
- `DELETE /api/empresas` - Deletar empresa (cascade: remove devedores, contratos, dívidas)

### Devedores

- `GET /api/devedores` - Listar devedores
- `GET /api/devedores/{id}` - Buscar devedor por ID
- `POST /api/devedores` - Criar devedor (cria conta automaticamente)
- `PUT /api/devedores/{id}` - Atualizar devedor
- `DELETE /api/devedores/{id}` - Deletar devedor (cascade: remove conta, contratos, dívidas)

### Contratos

- `GET /api/contratos` - Listar contratos
- `GET /api/contratos/{id}` - Buscar contrato por ID
- `POST /api/contratos` - Criar contrato
  - Campo `dataVencimento` é opcional (padrão: 1 ano a partir de hoje)
  - Status inicial: `ATIVO`
- `PUT /api/contratos/{id}` - Atualizar contrato
- `DELETE /api/contratos/{id}` - Deletar contrato

**Status de Contrato:**
- `RASCUNHO` - Contrato em rascunho
- `ATIVO` - Contrato ativo
- `CONCLUIDO` - Contrato concluído
- `CANCELADO` - Contrato cancelado

### Dívidas

- `GET /api/dividas` - Listar dívidas
- `GET /api/dividas/{id}` - Buscar dívida por ID
- `GET /api/dividas/search?min={valor}&max={valor}&contaId={uuid}` - Buscar dívidas com filtros
- `GET /api/dividas/stats/sum-by-conta/{contaId}` - Soma de valores por conta
- `GET /api/dividas/stats/count` - Contar dívidas
- `POST /api/dividas` - Criar dívida
- `PUT /api/dividas/{id}` - Atualizar dívida
- `DELETE /api/dividas/{id}` - Deletar dívida

### Pagamentos

- `GET /api/pagamentos` - Listar pagamentos
- `GET /api/pagamentos/{id}` - Buscar pagamento por ID
- `GET /api/pagamentos/stats/count-by-metodo?metodo={metodo}` - Contar pagamentos por método
- `POST /api/pagamentos` - Criar pagamento
  - **Importante**: O valor do pagamento é automaticamente herdado da dívida associada
  - Não é necessário informar o valor no request
- `PUT /api/pagamentos/{id}` - Atualizar pagamento
- `DELETE /api/pagamentos/{id}` - Deletar pagamento

**Métodos de Pagamento:**
- `DINHEIRO`
- `CARTAO_CREDITO`
- `CARTAO_DEBITO`
- `PIX`
- `TRANSFERENCIA_BANCARIA`
- `BOLETO`

### Relatórios

- `GET /api/relatorios` - Listar relatórios
- `GET /api/relatorios/{id}` - Buscar relatório por ID
- `POST /api/relatorios` - Criar relatório
- `PUT /api/relatorios/{id}` - Atualizar relatório
- `DELETE /api/relatorios/{id}` - Deletar relatório
- `GET /api/relatorios/auditoria?inicio={data}&fim={data}` - Gerar relatório de auditoria

**Tipos de Relatório:**

1. **MANUAL**: Relatório manual com valor informado
2. **CONTA_ESPECIFICA**: Relatório automático de uma conta específica
   - Calcula: saldo, total de dívidas, total de pagamentos, quantidades
   - Requer: `contaId`
3. **CONSOLIDADO_EMPRESA**: Relatório consolidado de todas as contas da empresa
   - Calcula: totais de todas as contas
4. **PERIODO**: Relatório de movimentações em um período
   - Requer: `dataInicio` e `dataFim`
5. **INADIMPLENCIA**: Relatório de contas inadimplentes
   - Opcional: `valorMinimoInadimplencia`
6. **RECEBIMENTOS**: Relatório de recebimentos em um período
   - Requer: `dataInicio` e `dataFim`

### Notificações

- `GET /api/notificacoes` - Listar notificações
- `GET /api/notificacoes/{id}` - Buscar notificação por ID
- `POST /api/notificacoes` - Criar notificação
- `PUT /api/notificacoes/{id}` - Atualizar notificação
- `DELETE /api/notificacoes/{id}` - Deletar notificação

## 📊 Modelo de Dados

### Relacionamentos Principais

```
User (1) ──< (1) Empresa
                │
                ├──< (N) Devedor ──< (1) Conta
                │                        │
                │                        ├──< (N) Divida
                │                        │
                │                        └──< (N) Pagamento
                │
                ├──< (N) Contrato ──< (N) Divida
                │
                └──< (N) Divida (como fiadora)
```

### Entidades Principais

- **User**: Usuário do sistema
- **Empresa**: Empresa do usuário
- **Devedor**: Devedor da empresa (tem uma Conta automaticamente)
- **Conta**: Conta do devedor (calcula saldo dinamicamente)
- **Contrato**: Contrato entre empresa e devedor
- **Divida**: Dívida associada a uma conta e contrato
- **Pagamento**: Pagamento de uma dívida (valor herdado da dívida)
- **Relatorio**: Relatório financeiro
- **Notificacao**: Notificação da empresa

### Cálculo de Saldo

O saldo da conta é calculado dinamicamente:

```
Saldo = (Soma dos valores das Dívidas) - (Soma dos valores dos Pagamentos)
```

O valor do pagamento é sempre igual ao valor da dívida associada.

## 🧪 Testes

### Executar Todos os Testes

```bash
mvn test
```

### Executar Testes de Integração

```bash
mvn test -Dtest=*IntegrationTest
```

### Executar Testes Unitários

```bash
mvn test -Dtest=*ServiceTest
```

### Cobertura de Testes

Os testes incluem:
- Testes unitários dos serviços
- Testes de integração com banco H2 em memória
- Testes de cascade deletion
- Testes de cálculos automáticos

## 🚢 Deploy

### Build para Produção

```bash
mvn clean package -DskipTests
```

### Variáveis de Ambiente

Configure as seguintes variáveis de ambiente em produção:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/orlando
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=senha
JWT_SECRET=chave-secreta-forte-e-aleatoria
JWT_EXPIRATION=3600000
```

### Docker (Exemplo)

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/projeto-orlando-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔧 Troubleshooting

### Erro: "No qualifying bean of type 'PasswordEncoder'"

Certifique-se de que o perfil não está como "test" em produção. O `PasswordEncoderConfig` está configurado para não carregar no perfil de teste.

### Erro: "ObjectOptimisticLockingFailureException" ao deletar empresa

O método `delete()` da empresa foi ajustado para usar `deleteById()` e garantir a ordem correta das operações de cascade.

### Erro: Lazy loading em cálculos de saldo

Os serviços usam `Hibernate.initialize()` para garantir que as coleções sejam carregadas antes dos cálculos.

## 📝 Licença

Este projeto está sob a licença Apache 2.0.

## 👥 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📞 Contato

- **Email**: devleo@tech.com
- **Documentação**: http://localhost:8080/swagger-ui.html

---

**Desenvolvido com ❤️ por DevLeo**

