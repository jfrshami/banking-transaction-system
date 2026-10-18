# Repository instructions

## Scope and architecture

- This is a Java 25 / Spring Boot 4.1 modular monolith, built with Maven Wrapper.
- Use base package `com.jfrshami.banking` and organize by feature: `account`,
  `transaction`, `ledger`, `outbox`, and `common`. These are Java package boundaries
  in one Maven module and one deployment, not separate services.
- Keep each feature's persistence and implementation internal. When collaboration
  is needed, expose a small explicit application API; do not import another
  feature's repositories or entities. Avoid dependency cycles.
- `common` contains only small domain-neutral primitives and must not depend on
  any feature. Do not turn it into a shared business-logic layer.
- Add subpackages within a feature only when actual code needs them. Do not add
  speculative layers, generic base services, or frameworks to enforce empty modules.
- Do not introduce banking logic, entities, banking tables, Kafka, or microservices
  unless a future task explicitly requests them.

## Coding and dependencies

- Use four-space indentation, clear names, constructor injection, and `jakarta.*`
  APIs. Avoid deprecated APIs, field injection, Lombok, and unnecessary dependencies.
- Use the Spring Boot parent to manage compatible Spring Framework 7 and library
  versions. Prefer Boot 4's focused starters, such as `spring-boot-starter-webmvc`
  and `spring-boot-starter-flyway`.
- JUnit 5.14.4 is explicitly selected to satisfy the project's JUnit 5 requirement.
  Spring Framework 7's `SpringExtension` requires JUnit 6. Do not add
  `@SpringBootTest`, Spring test slices, or `SpringExtension` while retaining JUnit 5;
  use a directly managed application context, or discuss a JUnit upgrade if needed.
- Keep credentials out of code, examples, logs, and Git. `.env` is local and ignored.
  Supply production secrets through the deployment environment or a secret store.
- Expose only Actuator health by default, without details. Review authentication
  and authorization requirements when adding business HTTP endpoints.

## Persistence

- PostgreSQL 17 is the database. Flyway owns schema evolution; migrations belong
  in `src/main/resources/db/migration` as `V<version>__<description>.sql`.
- Never edit a migration that has been applied to a shared environment.
- Keep Hibernate `ddl-auto=validate`, Open Session in View disabled, and Flyway
  clean disabled. Do not enable `create`, `create-drop`, or `update`.
- No SQL migrations are needed until the first persistence feature is requested.

## Testing and verification

- Run `.\mvnw.cmd clean verify` on Windows or `./mvnw clean verify` on POSIX with
  JDK 25 after meaningful Java, dependency, or configuration changes.
- The default smoke test must remain runnable without Docker, credentials, or an
  external database. It starts the real HTTP server and verifies Actuator health;
  its persistence exclusions must never leak into main configuration.
- For future persistence behavior, use PostgreSQL 17 Testcontainers, not H2.
  Make Docker requirements explicit; do not silently skip required integration tests.
- Add focused behavior tests for new functionality and regression tests for fixes.
  Keep integration tests isolated and deterministic; do not access shared databases.
- When editing Compose configuration, run `docker compose config --quiet` and,
  when Docker is available, verify database startup and application connectivity.
- Report exact commands, test counts, failures, and environmental limitations.
  Never claim a build or Docker check passed if it was not run successfully.

## Future Codex tasks

- Inspect Git status and existing instructions before editing. Preserve unrelated
  user changes, the LICENSE, and existing README and .gitignore content.
- Keep changes scoped, minimal, and reviewable. Update setup documentation when
  commands or configuration change. Do not commit or push unless explicitly asked.
- Use the wrapper; do not require a global Maven installation. Do not downgrade
  the requested Java or Spring versions to work around local environment problems.
- Inspect existing API contracts before changing HTTP behavior. Follow applicable
  API engineering skills without adding remote workspaces or publishing artifacts
  unless the task needs them and the user has authorized publication.
