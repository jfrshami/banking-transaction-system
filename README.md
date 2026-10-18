# banking-transaction-system
A production-oriented banking backend built with Java, Spring Boot, PostgreSQL, and Kafka, focusing on transaction integrity, concurrency control, and event-driven architecture.

The current repository is the application foundation. Banking business logic,
entities, banking tables, and Kafka integration are planned; none are implemented yet.

## Stack and prerequisites

- JDK 25 (LTS); make sure `java -version` reports 25 and `JAVA_HOME`, if set,
  points to that JDK.
- Spring Boot 4.1.1 with its managed Spring Framework 7 dependencies.
- Maven 3.9.12 via the included Maven Wrapper 3.3.4; no global Maven is required.
  Internet access to Maven Central is needed for the initial dependency download.
- Docker Engine/Desktop with Docker Compose v2 for local PostgreSQL 17.
- JUnit Jupiter 5.14.4 and Boot-managed Testcontainers for testing.

Boot 4.1.1 is the latest stable 4.1 patch verified for this scaffold. See the
[Spring Boot requirements](https://docs.spring.io/spring-boot/system-requirements.html).
Boot normally manages JUnit 6. This project explicitly uses JUnit 5; the smoke
test starts Spring directly because Spring Framework 7's `SpringExtension`
requires JUnit 6.

## Local setup

1. Copy `.env.example` to `.env` (`Copy-Item .env.example .env` in PowerShell,
   or `cp .env.example .env` on POSIX).
2. Fill in `DB_USERNAME` and `DB_PASSWORD` with your own local values. They have
   no defaults. Keep `.env` private; it is ignored by Git.
3. Start PostgreSQL from the repository root:

   ```sh
   docker compose up -d --wait
   ```

Compose reads `.env` automatically. A Java process does not: export the same
values into the shell used to run the application. For the simple, unquoted
`KEY=value` format in `.env.example`, PowerShell can load them as follows:

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^([A-Z_]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}
.\mvnw.cmd spring-boot:run
```

Use values without `$` interpolation or quoting when sharing this simple format
between Compose and that PowerShell loader. Alternatively, set the variables
directly in your shell or IDE. On POSIX, a shell-compatible `.env` can be loaded
with `set -a; . ./.env; set +a`, then run `./mvnw spring-boot:run`. If your checkout
does not mark the script executable, run `sh mvnw spring-boot:run`.

| Variable | Purpose | Default |
| --- | --- | --- |
| `DB_USERNAME` | Application and local PostgreSQL user | Required |
| `DB_PASSWORD` | Application and local PostgreSQL password | Required |
| `DB_URL` | JDBC URL used by the application | `jdbc:postgresql://localhost:5432/banking` |
| `POSTGRES_DB` | Database initialized by Compose | `banking` |
| `POSTGRES_PORT` | PostgreSQL port bound to loopback on the host | `5432` |
| `SERVER_PORT` | Application HTTP port | `8080` |

If you change `POSTGRES_DB` or `POSTGRES_PORT`, update `DB_URL` to match. Compose
credentials and database settings initialize an **empty** volume only; changing
them does not change an existing database's users or passwords.

The database volume persists across `docker compose down` and restarts. Use
`docker compose logs postgres` for database logs and `docker compose down` to
stop local infrastructure. Deleting the volume destroys local data.

## Health and persistence

Once PostgreSQL and the application are running:

```sh
curl http://localhost:8080/actuator/health
```

The endpoint returns HTTP 200 containing `"status":"UP"` when healthy (health
group names may also be present). Only health
is exposed, without component details. Normal runtime health includes database
connectivity; database failures can produce HTTP 503 and a `DOWN` status.

Flyway is enabled and owns schema changes under `src/main/resources/db/migration`.
There are no SQL migrations or banking tables yet. Flyway may create its own
schema history table at startup. Hibernate uses `ddl-auto=validate`, never schema
creation or updates, and Open Session in View is disabled. Compose is for local
development; deployed environments supply their own PostgreSQL URL and secrets.

## Build and test

```powershell
.\mvnw.cmd clean verify
java -jar target/banking-transaction-system-0.0.1-SNAPSHOT.jar
```

On POSIX, use `./mvnw clean verify`. The packaged application uses the same
environment variables as `spring-boot:run`.

The default test starts the application on a random port and asserts the actual
health HTTP response. It excludes JDBC, JPA, and Flyway auto-configuration only
within the test, so `clean verify` requires neither Docker nor database credentials.
It does not verify persistence. Testcontainers PostgreSQL and JUnit integration
dependencies are included for future database tests; none run containers yet.

On Windows hosts where JDK 25 reports `Unable to establish loopback connection`
with `UnixDomainSockets` / `Invalid argument: connect`, use an explicit writable
socket temporary directory for the test JVM. For this repository location:

```powershell
.\mvnw.cmd '-DargLine=-Djdk.net.unixdomain.tmpdir=D:/personal-project/banking-transaction-system/target' clean verify
```

Adjust that absolute path for your checkout. This is a host workaround, not an
application setting. If it also affects running the packaged application, pass
`-Djdk.net.unixdomain.tmpdir=<existing-writable-directory>` to `java` before `-jar`.

## Architecture and contribution

One Maven artifact and one process form a modular monolith. Feature packages under
`com.jfrshami.banking` are `account`, `transaction`, `ledger`, `outbox`, and `common`.
Their `package-info.java` files reserve boundaries without speculative business code.
Features will collaborate through explicit application APIs, keeping repositories
and entities internal. `common` must remain domain-neutral and independent of features.
These boundaries are conventions at this stage, not automatically enforced.

See [AGENTS.md](AGENTS.md) for coding rules, migration policy, testing requirements,
and instructions for future Codex tasks. The existing [MIT license](LICENSE) applies.
