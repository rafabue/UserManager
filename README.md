# User Manager

Aplicação web para cadastro, edição e consulta de usuários. API REST em Spring Boot e interface em
Vue 3.

## Stack

Java 17, Spring Boot 3.5 (Web, Security, Data JPA, Validation), H2 com Flyway, springdoc-openapi.
Frontend em Vue 3 com TypeScript, Vite, Vue Router e Pinia.

## Como executar

O build precisa de JDK 17 e Node 22. O wrapper do Maven dispensa a instalação prévia do Maven.

A API:

```bash
cd backend
./mvnw spring-boot:run
```

Sobe em `http://localhost:8080`

A interface, em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Sobe em `http://localhost:5173`, com proxy de `/api` para a porta 8080 — o que dispensa configurar
CORS em desenvolvimento. A API precisa estar no ar.

Para gerar e executar o jar:

```bash
cd backend
./mvnw clean package -Pfrontend
java -jar target/user-manager-1.0.0.jar
```

O profile `frontend` compila o Vue e embute o bundle no jar, resultando em um artefato único que
serve a interface na raiz e a API em `/api`. Sem ele, `./mvnw clean package` entrega apenas a API, e
o build não depende de Node.

O banco é um arquivo H2 em `backend/data/usermanager.mv.db`, criado na primeira execução. Para
voltar ao estado inicial, pare a aplicação e apague o arquivo — o Flyway recria o schema e roda as
migrations na próxima subida:

```bash
rm backend/data/usermanager.mv.db
```

Testes:

```bash
cd backend
./mvnw test
```

## Acesso

HTTP Basic, com usuário técnico. Padrão local: `admin` / `admin123`. Configurável por variáveis de
ambiente, conforme o `.env.example`. São as mesmas credenciais da tela de login da interface, que
as mantém apenas em memória e as envia no header `Authorization` de cada requisição.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/users` | Cadastra um usuário (nome, e-mail, senha) |
| `GET` | `/api/users` | Lista paginada, com filtro opcional `name` por qualquer parte do nome |
| `GET` | `/api/users/{id}` | Consulta por id |
| `PUT` | `/api/users/{id}` | Edita nome e e-mail |

Swagger UI em `http://localhost:8080/swagger-ui.html`

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

## Possibilidades para o frontend

Foi adotado Vue 3 devido ao conhecimento relacionado ao framework e facilidade de integração com Spring.

O bundle é embutido no jar pelo profile do Maven `frontend`, o que resulta em **um único artefato de
deploy**. O build padrão não depende de Node, de modo que a API possa ser avaliada sem o front presente.

Uma observação sobre a implementação, o estado das credenciais em tela são **em memória**, no store do Pinia, não fica no `localStorage` nem
no `sessionStorage`. Foi intencional, como a autenticação é HTTP Basic, a credencial enviada em
toda requisição é a própria senha, apenas codificada em base64, que é reversível, não é
criptografia. 

A consequência é que a sessão se perde ao recarregar a página. Considerei o novo login um incômodo
menor do que essa exposição. A solução que evitaria as duas coisas seria trocar o Basic por sessão
com cookie `HttpOnly`, o que exigiria um endpoint de sessão no backend, que foge do escopo da entrega.