# MCP-TODO — skill vs. MCP for oppslag i EUs SPARQL-endepunkt

Notat for senere. Konklusjon: en **skill holder** for oppslag. MCP er overkill nå, men
kan vurderes hvis oppslaget skal gjenbrukes som et typet verktøy på tvers av klienter.

## Hvorfor en skill er nok

EUs endepunkt (`https://publications.europa.eu/webapi/rdf/sparql`) er et åpent HTTP
SPARQL-endepunkt. Et oppslag er én HTTP-forespørsel + parsing — ingen state, ingen
hemmeligheter, offentlig data. En skill kan pakke alt som trengs:

- den kanoniske ISG→ISO-spørringen (samme som `src/main/resources/ISG-TO-ISO.sparql`)
- et lite script (curl/python/RDF4J) som kjører spørringen
- instruksjoner for hvordan agenten kaller og tolker svaret, inkl. UK/EL-avvikene

## Når MCP faktisk lønner seg

Velg MCP kun hvis minst én av disse gjelder:

- **Gjenbruk på tvers av klienter** (Copilot i IDE, Claude Desktop, CI-agent) uten å
  kopiere skript
- **Caching/robusthet** — endepunktet er tregt og tidvis ustabilt; du vil ha cache,
  retries, rate-limiting
- **Stabil kontrakt** — du vil skjule SPARQL bak et typet API med input-validering

## Hva en MCP i så fall skulle gjort

Eksponere noen få typede verktøy og gjøre SPARQL-jobben internt:

| Verktøy | Inn | Ut |
|---------|-----|-----|
| `lookup_country_by_isg` | `isg` (f.eks. `UK`) | `{isg, isoAlpha2, isoAlpha3, labelEn, labelNo, uri}` |
| `lookup_country_by_iso` | `iso` (f.eks. `GB`) | samme, invers |
| `list_country_mappings` | – | hele tabellen (cachet) |

Internt: bygger SPARQL, kaller endepunktet, parser SPARQL-JSON, cacher resultater, og
normaliserer EESSI-avvikene (UK↔GB, EL↔GR).

## Anbefaling for dette repoet

Vi har allerede Java-tjenesten som gjør nøyaktig dette. For agent-oppslag er en **skill
som gjenbruker samme SPARQL** det pragmatiske valget nå (se `skills/eessi-country-lookup/`).
Bygg MCP først når gjenbruk på tvers av klienter blir et reelt behov.
