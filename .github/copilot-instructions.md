# Copilot instructions for `tk2-country-code`

Spring Boot 3.5 / Java 21 service that maps country codes used in EESSI (ISG_COU)
to ISO 3166-1 alpha-2 and alpha-3, by querying the EU Publications Office SPARQL
endpoint with RDF4J.

## Build, test, run

Use the Maven Wrapper (`./mvnw`), not a locally installed Maven.

- Build: `./mvnw clean package`
- Full test suite: `./mvnw test`
- Single test class: `./mvnw -Dtest=CountryCodeServiceTest test`
- Single test method: `./mvnw -Dtest=CountryCodeServiceTest#methodName test`
- Run locally: `./mvnw spring-boot:run`

## Architecture

The whole service is a thin wrapper around one external data source:
`https://publications.europa.eu/webapi/rdf/sparql` (the EU Publications Office
country authority table).

- RDF4J (`rdf4j-storage`, BOM-managed at `rdf4j-bom` 5.1.6) evaluates SPARQL and
  parses results. Use the existing idioms — `prepareQuery`,
  `preparedQuery.setBinding(...)`, `TupleQueryResult` — instead of adding a
  second query layer.
- The core logic is the SPARQL query plus its result mapping. Keep query text,
  bindings, and result parsing together so the mapping rules stay legible.
- Domain rules that cannot be inferred from code: EESSI uses non-ISO codes for
  two states — `UK` (ISO `GB`) and `EL` (ISO `GR`). The mapping exists to
  reconcile these. The SPARQL query filters to the `EU_EFTA_UK` use-context and
  excludes the `EUR` and `OP_DATPRO` pseudo-entries. Preserve these filters when
  editing queries.
- Spring Web provides HTTP; Jackson handles JSON; Lombok is used via annotation
  processing (excluded from the boot fat-jar).

## Conventions

- `IAEG.md` is the source of truth for the domain problem and holds the canonical
  SPARQL query (with a `REPLACE_ME` binding placeholder for the ISG_COU value).
  Keep code and `IAEG.md` aligned when mapping or query behavior changes.
- Package root is `no.tk2.eessi` (artifact `no.tk2.eessi:tk2-country-code`).
- `target/` is build output — never commit it.
