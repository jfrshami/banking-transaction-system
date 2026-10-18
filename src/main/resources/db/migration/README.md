# Database migrations

Add versioned Flyway SQL migrations here when persistence is introduced, for
example `V1__create_accounts.sql`. Do not edit an applied migration; add a new one.
There are intentionally no SQL migrations or banking tables in this scaffold.
Flyway creates its own schema history table when the application first starts
against PostgreSQL. Hibernate validates mappings and never creates the schema.
