# E-commerce full stack

Loja virtual com backend em **Java 21 + Spring Boot 4**, frontend em **React + TypeScript (Vite)**, banco **PostgreSQL 17** e infraestrutura com **Docker**.

```
ecommerce/
├── backend/            API REST (Spring Boot, package-by-feature)
├── frontend/           SPA React + TypeScript
├── docker-compose.yml  Postgres (sempre) + backend/frontend (perfil "app")
└── .env.example        Modelo das variáveis de ambiente
```

## Como rodar em desenvolvimento

Pré-requisitos: Java 21, Node 24 e Docker.

**1. Banco**

```powershell
docker compose up -d          # sobe só o PostgreSQL
```

**2. Variáveis de ambiente.** O Spring **não** lê o `.env`; defina as variáveis no sistema ou na Run Configuration da IDE. Uma vez, no PowerShell:

```powershell
# Secret do JWT (obrigatória, mínimo 32 bytes)
$b = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
[Environment]::SetEnvironmentVariable('SECURITY_JWT_SECRET', [Convert]::ToBase64String($b), 'User')
```

Para criar o primeiro administrador, defina também `ADMIN_EMAIL` e `ADMIN_PASSWORD` (mínimo 8 caracteres) na sessão antes de subir o backend:

```powershell
$env:ADMIN_EMAIL = 'admin@exemplo.com'
$env:ADMIN_PASSWORD = 'uma-senha-forte'
```

Abra um terminal novo (ou reinicie a IDE) depois de criar variáveis de usuário.

**3. Backend** (porta 8080)

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**4. Frontend** (porta 5173; o Vite repassa `/api` para o backend)

```powershell
cd frontend
npm install
npm run dev
```

Acesse http://localhost:5173.

## Testes

```powershell
cd backend
.\mvnw.cmd test        # precisa do Postgres rodando (testes de integração usam o banco real)

cd ..\frontend
npm run build          # checagem de tipos + build
npm run lint
```

Os testes usam uma secret fictícia (`src/test/resources/config/application.properties`) e o teste de integração desfaz tudo o que grava (`@Transactional`).

## Personalizar a loja para um cliente

O visual é neutro de propósito, para servir a qualquer tipo de produto. Para adaptar:

| O quê | Onde |
|---|---|
| Nome, slogan, textos do banner, destaques e aviso do topo | `frontend/src/brand.ts` |
| Cor da marca (botões, contador do carrinho, destaques) | variáveis `--brand*` no topo de `frontend/src/index.css` |
| Ícone da aba do navegador | `frontend/public/favicon.svg` (use a mesma cor de `--brand`) |
| Título padrão da aba | `<title>` em `frontend/index.html` |

A logo do cabeçalho é gerada a partir do nome (inicial em um quadrado na cor da marca). Para usar a logo real do cliente, troque o conteúdo de `frontend/src/components/Logo.tsx` por uma imagem.

## Rodando tudo em containers

```powershell
copy .env.example .env   # preencha SECURITY_JWT_SECRET (o Compose lê o .env da raiz)
docker compose --profile app up -d --build
```

Frontend em http://localhost:3000 (Nginx, que repassa `/api` para o backend).

## Variáveis de ambiente

| Variável | Obrigatória | Padrão | Uso |
|---|---|---|---|
| `SECURITY_JWT_SECRET` | sim | — | Assina e valida os JWT (HS256) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | não | vazio | Cria o admin inicial se o e-mail não existir |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | não | Postgres local do compose | Conexão com o banco |
| `CORS_ALLOWED_ORIGINS` | não | `http://localhost:5173` | Origens do frontend autorizadas |
| `PAYMENT_PROVIDER` | não | `fake` | Provider de pagamento ativo |
| `STORAGE_LOCAL_DIR` | não | `uploads` | Pasta das imagens enviadas |
| `JPA_SHOW_SQL` | não | `true` | Exibe o SQL no log |

## API

Autenticação: `Authorization: Bearer <token>` (obtido em `POST /api/auth/login`, válido por 2h).

| Recurso | Endpoints | Acesso |
|---|---|---|
| Login / cadastro | `POST /api/auth/login`, `POST /api/users` | público |
| Catálogo | `GET /api/categories[/{id}]`, `GET /api/products[/{id}]` | público |
| Catálogo (escrita) | `POST/PUT/DELETE` em `/api/categories` e `/api/products` | ADMIN |
| Estoque | `GET /api/products/{id}/stock` | público |
| | `POST /api/products/{id}/stock/increase` · `/decrease` | ADMIN |
| Imagens | `GET /api/products/{id}/images`, `GET /api/images/{id}` | público |
| | `POST /api/products/{id}/images` (multipart `file`), `DELETE .../images/{imageId}` | ADMIN |
| Usuário logado | `GET /api/users/me` | autenticado |
| Carrinho | `GET/DELETE /api/cart`, `POST /api/cart/items`, `PUT/DELETE /api/cart/items/{productId}` | autenticado |
| Endereços | `GET/POST /api/addresses`, `GET/PUT/DELETE /api/addresses/{id}`, `PATCH /api/addresses/{id}/default` | autenticado (só os próprios) |
| Pedidos | `POST /api/orders` (checkout), `GET /api/orders[/{id}]`, `POST /api/orders/{id}/cancel` | autenticado (só os próprios) |
| Pagamentos | `POST/GET /api/orders/{id}/payments` | autenticado (só os próprios) |
| Admin | `GET /api/users[/{id}]`, `PATCH /api/admin/users/{id}/active`, `GET /api/admin/orders[?status=]`, `GET /api/admin/orders/{id}`, `PATCH /api/admin/orders/{id}/status` | ADMIN |
| Saúde | `GET /actuator/health` | público |

Erros seguem o formato `{ status, message, timestamp, errors }`: 400 validação, 401 não autenticado, 403 sem permissão, 404 não encontrado (inclusive recurso de outro usuário), 409 conflito de dados, 413 arquivo grande, 422 regra de negócio.

## Decisões de arquitetura

- **Package-by-feature**: cada pasta (`stock`, `cart`, `order`...) tem entidade, repositório, service, controller e DTOs.
- **Regras na entidade**: `Stock.decrease`, `Cart.addItem`, `Order.changeStatus` validam a si mesmas; o service orquestra.
- **Estoque consistente**: `SELECT ... FOR UPDATE` na baixa, `CHECK (quantity >= 0)` no banco e locks sempre na mesma ordem (id do produto) para evitar deadlock.
- **Checkout em uma transação**: baixa de estoque, criação do pedido e limpeza do carrinho; qualquer falha desfaz tudo.
- **Preço e endereço congelados** no pedido (`order_items.unit_price`, colunas `shipping_*`).
- **Máquina de estados** do pedido em `OrderStatus.allowedNext()`; cancelar devolve o estoque.
- **Pagamento desacoplado**: `PaymentProvider` (interface) + `PaymentProviderResolver` (escolhe pela config). O valor cobrado vem sempre do banco, nunca do cliente; índice único impede dois pagamentos aprovados no mesmo pedido.
- **Storage desacoplado**: `StorageService` (interface) com `LocalStorageService`; tipo do arquivo validado por *magic bytes*; chaves geradas pelo servidor (sem path traversal).
- **Segurança**: JWT stateless, roles verificadas no backend, recursos do usuário sempre buscados por `id + userId`, admin inicial via variáveis de ambiente (nunca em migration).

## Limitações conhecidas / próximos passos

- Usuário desativado mantém um token já emitido até expirar (até 2h).
- Boleto (provider fake) fica `PENDING` para sempre: falta um webhook de confirmação.
- Com gateway real, a chamada ao provider deve sair de dentro da transação (registrar tentativa → chamar gateway → confirmar por webhook).
- Apagar um produto não apaga os arquivos de imagem do disco (as linhas somem por `ON DELETE CASCADE`).
- O catálogo faz uma requisição de imagens por produto; com muitos produtos, incluir a imagem principal no `ProductResponse` e paginar `GET /api/products`.
- `spring.jpa.open-in-view` está no padrão (ligado); desligá-lo exige revisar os services do catálogo, que leem a categoria (LAZY) fora de transação.
