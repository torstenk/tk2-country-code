# tk2-country-code

Slår opp mapping mellom EESSI sine ISG-landkoder og ISO 3166-1 (alpha-2/alpha-3) ved å
spørre EUs Publications Office SPARQL-endepunkt
(`https://publications.europa.eu/webapi/rdf/sparql`).

EESSI bruker to landkoder som avviker fra ISO 3166-1:

- **UK** i EESSI = **GB** i ISO (Storbritannia)
- **EL** i EESSI = **GR** i ISO (Hellas)

## Bygg og kjør

Spring Boot 4.1 / Java 25. Bruk Maven Wrapper:

```bash
./mvnw clean package        # bygg
./mvnw test                 # kjør tester
./mvnw spring-boot:run      # kjør appen
```

## Slik gjør tjenesten oppslaget

`CountryCodeService` laster SPARQL-spørringen fra
`src/main/resources/ISG-TO-ISO.sparql`, binder ISG-koden og evaluerer mot endepunktet
med RDF4J:

```java
TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
query.setBinding("isgCouValue", conn.getValueFactory().createLiteral("UK"));
TupleQueryResult result = query.evaluate();
```

## Skill: eessi-country-lookup

For agent-oppslag (uten å kjøre Java) finnes en skill i
[`skills/eessi-country-lookup/`](skills/eessi-country-lookup/). Den kjører samme
mapping via et lite `curl`-skript mot SPARQL-endepunktet.

```bash
# Etter EESSI ISG-kode
skills/eessi-country-lookup/scripts/lookup.sh --isg UK

# Etter ISO-kode (alpha-2 eller alpha-3)
skills/eessi-country-lookup/scripts/lookup.sh --iso GB

# Hele mappingtabellen
skills/eessi-country-lookup/scripts/lookup.sh --all
```

Krever `curl`; tabellformat krever `jq` (bruk `--format json` uten). Se
[`skills/eessi-country-lookup/SKILL.md`](skills/eessi-country-lookup/SKILL.md) for
detaljer.

> Skill vs. MCP: se [`MCP-TODO.md`](MCP-TODO.md) for vurderingen av når et oppslag bør
> pakkes som MCP i stedet.
