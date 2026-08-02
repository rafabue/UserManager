# User Manager

Aplicação web com endpoints REST para cadastro, edição e consulta de usuários.

## Stack

Java 17, Spring Boot 3.5 (Web, Security, Data JPA, Validation), H2 com Flyway, springdoc-openapi.

## Como executar

JDK 17. O wrapper do Maven dispensa a instalação prévia do Maven.

```bash
cd backend
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. Não há interface: a raiz responde 404, e o ponto de
entrada é o Swagger UI. A notificação de e-mail aparece no terminal a cada cadastro ou edição.

Para gerar e executar o jar:

```bash
cd backend
./mvnw clean package
java -jar target/user-manager-1.0.0.jar
```

Testes:

```bash
cd backend
./mvnw test
```

## Acesso

HTTP Basic, com usuário técnico. Padrão local: `admin` / `admin123`. Configurável por variáveis de
ambiente, conforme o `.env.example`.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/users` | Cadastra um usuário (nome, e-mail, senha) |
| `GET` | `/api/users` | Lista paginada, com filtro opcional `name` por qualquer parte do nome |
| `GET` | `/api/users/{id}` | Consulta por id |
| `PUT` | `/api/users/{id}` | Edita nome e e-mail |

Swagger UI em `http://localhost:8080/swagger-ui.html`.

## Testes

São 35 testes automatizados. O critério foi cobrir o que tem lógica própria — ordem de operações,
regras de unicidade, tratamento da entrada e contrato HTTP — e deixar de fora o que apenas
repetiria a implementação.

`UserServiceTest` — 15 testes com Mockito. Regras de negócio isoladas: ordem entre persistência e
notificação, unicidade do e-mail no cadastro e na edição, tratamento do filtro por nome e escape
dos curingas do `LIKE`.

`UserServiceIntegrationTest` — 3 testes com `@SpringBootTest`. Verifica contra o banco o que o mock
não alcança: o rollback do cadastro quando a notificação falha, o caminho feliz equivalente e o
curinga tratado como texto literal na consulta.

`UserControllerTest` — 12 testes com `@WebMvcTest`. Contrato HTTP: 201 com `Location`, 400 de
validação, 409, 404, formato de paginação, ausência da senha nas respostas e o limite de 72 bytes
imposto pelo BCrypt.

`SecurityConfigTest` — 5 testes. Separação entre a cadeia de filtros da API e a da documentação,
com 401 para requisições sem credenciais ou com credenciais inválidas.