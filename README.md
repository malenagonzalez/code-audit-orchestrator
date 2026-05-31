# code-audit-orchestrator

Servicio orquestador de la **Plataforma de Auditoría de Código**.
Es el único punto de acceso al sistema: valida JWT, persiste datos y delega el análisis al servicio de IA (Repo C).

## Stack

- Java 17 / Spring Boot 3.3.5
- Spring Security (stateless, JWT via `jjwt 0.12.x`)
- Spring Data JPA + Hibernate 6 + SQLite
- Flyway para migraciones de esquema
- WebClient (llamadas al servicio Python)
- Lombok, Bean Validation
- springdoc-openapi (Swagger UI)

## Requisitos

- JDK 17+
- Maven (wrapper incluido: `./mvnw`)

## Correr

```bash
./mvnw spring-boot:run
```

La base de datos SQLite se crea automáticamente en `./data/codeaudit.db` al primer arranque.

## Variables de entorno

Todas tienen defaults para desarrollo local:

| Variable | Default | Descripción |
|----------|---------|-------------|
| `JWT_SECRET` | `change-me-in-production-min-32-chars!!` | Secret HMAC-SHA256 para firmar JWT (mínimo 32 chars) |
| `JWT_EXPIRATION` | `86400000` | Expiración del token en ms (24h) |
| `AI_SERVICE_URL` | `http://localhost:8000` | URL del servicio Python (Repo C) |
| `AI_SERVICE_API_KEY` | `changeme` | Shared secret con Repo C |
| `DB_PATH` | `./data/codeaudit.db` | Ruta del archivo SQLite |

## Endpoints

### Auth (públicos)

| Método | Ruta | Body | Respuesta |
|--------|------|------|-----------|
| POST | `/api/auth/register` | `{ username, email, password }` | `{ token, email, username }` |
| POST | `/api/auth/login` | `{ email, password }` | `{ token, email, username }` |

### Auditorías (requieren `Authorization: Bearer <token>`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/audits` | Analizar un snippet de código |
| GET | `/api/audits?page=0&size=10` | Historial paginado del usuario |
| GET | `/api/audits/{id}` | Detalle de una auditoría |
| DELETE | `/api/audits/{id}` | Eliminar una auditoría |

### Otros

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/health` | Health check (público) |
| GET | `/swagger-ui.html` | Documentación interactiva |
| GET | `/v3/api-docs` | OpenAPI JSON |

## Decisiones de diseño

### SQLite sobre PostgreSQL
Elimina la necesidad de un proceso separado o Docker para el TP. El archivo `codeaudit.db` se crea solo.

### JWT stateless
Tokens HMAC-SHA256 firmados con `JWT_SECRET`. El servidor no mantiene sesión — cada request se valida independientemente. Expira en 24h.

### LocalDateTime → SQLite TEXT
SQLite no tiene tipo `TIMESTAMP` nativo. Se usa `LocalDateTimeConverter` (`@Converter(autoApply = true)`) que serializa `LocalDateTime` como ISO-8601 en columnas `TEXT`. Sin este converter, Hibernate 6 genera un `Error parsing time stamp` al leer registros, lo que rompe la autenticación JWT (porque `loadUserByUsername` falla antes de setear el `SecurityContext`).

### Flyway en lugar de `ddl-auto: create`
Migraciones versionadas en `src/main/resources/db/migration/`:
- `V1__create_users_table.sql`
- `V2__create_audit_records_table.sql`
- `V3__create_audit_issues_table.sql`

Hibernate valida el esquema contra las entidades al arrancar (`ddl-auto: validate`).

### CORS + Spring Security
El preflight CORS del navegador envía `OPTIONS` antes de cada POST cross-origin. Con auth stateless, `OPTIONS` debe ser permitido explícitamente:

```java
.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
```

Sin esta regla, el registro y login dan 403 antes de llegar al endpoint.

### Seguridad entre servicios
Repo C no está expuesto públicamente. Solo acepta requests con `X-Internal-Api-Key`. El valor se configura con `INTERNAL_API_KEY` en ambos servicios — deben coincidir.

## Estructura de paquetes

```
com.auditoria.orchestrator/
├── config/      SecurityConfig, WebClientConfig, LocalDateTimeConverter
├── controller/  AuthController, AuditController, HealthController
├── dto/
│   ├── request/ LoginRequest, RegisterRequest, AuditRequest
│   └── response/ AuthResponse, AuditResultDto, AuditSummaryDto, AuditIssueDto
├── entity/      User, AuditRecord, AuditIssue
├── exception/   GlobalExceptionHandler, NotFoundException, ConflictException
├── repository/  UserRepository, AuditRecordRepository
├── security/    JwtTokenProvider, JwtAuthFilter, UserDetailsServiceImpl
└── service/     AuthService, AuditService, AiInferenceClient
```
