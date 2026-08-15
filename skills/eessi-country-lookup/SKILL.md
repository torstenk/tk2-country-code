---
name: eessi-country-lookup
description: Slå opp EESSI ISG-landkoder mot ISO 3166-1 (alpha-2/alpha-3) via EUs SPARQL-endepunkt. Bruk når du trenger å mappe en EESSI-landkode til ISO-kode eller omvendt, f.eks. UK↔GB eller EL↔GR, eller liste hele mappingtabellen.
license: MIT
metadata:
  domain: eessi
  tags: sparql country-code iso-3166 eessi rdf
---

# EESSI country-code lookup

Slå opp mapping mellom EESSI sine ISG-landkoder og ISO 3166-1 (alpha-2/alpha-3) ved å
spørre EUs Publications Office SPARQL-endepunkt
(`https://publications.europa.eu/webapi/rdf/sparql`).

EESSI bruker noen landkoder som avviker fra ISO 3166-1:

- **UK** i EESSI = **GB** i ISO (Storbritannia)
- **EL** i EESSI = **GR** i ISO (Hellas)

Endepunktet er åpent (ingen autentisering), så oppslaget er én HTTP-forespørsel.

## Når du skal bruke denne

- Brukeren spør «hva er ISO-koden for EESSI-kode X?» eller omvendt
- Du trenger å normalisere en landkode før lagring eller utveksling
- Du vil ha hele ISG→ISO-tabellen

## Slik gjør du oppslaget

Kjør skriptet `scripts/lookup.sh` (krever `curl`; tabellformat krever `jq`):

```bash
# Etter EESSI ISG-kode
scripts/lookup.sh --isg UK

# Etter ISO-kode (alpha-2 eller alpha-3)
scripts/lookup.sh --iso GB

# Hele mappingtabellen
scripts/lookup.sh --all

# Rå SPARQL-JSON i stedet for tabell
scripts/lookup.sh --isg EL --format json
```

Eksempel på svar (`--isg UK`):

```
ISG  ISO2  ISO3  LABEL           URI
UK   GB    GBR   United Kingdom  http://publications.europa.eu/resource/authority/country/GBR
```

## Slik tolker du svaret

- `ISG` — EESSI-koden (input/output for EESSI-utveksling)
- `ISO2` / `ISO3` — ISO 3166-1 alpha-2/alpha-3
- `LABEL` — engelsk landnavn
- `URI` — den autoritative EU-ressursen for landet

Pass alltid på UK/EL-avvikene: bruk ISG-koden mot EESSI, ISO-koden mot alt annet.

## Under panseret

`query/lookup.rq` er en parameterisert SPARQL-spørring. Skriptet setter inn et
`FILTER`-uttrykk (`##FILTER_CLAUSE##`) basert på oppslagstypen, filtrerer på
`EU_EFTA_UK`-konteksten og ekskluderer pseudo-oppføringene `EUR` og `OP_DATPRO`.
Landkoder valideres (`^[A-Z]{2,3}$`) før de settes inn, for å unngå SPARQL-injeksjon.

Dette er samme mapping som Java-tjenesten i dette repoet bruker
(`src/main/resources/ISG-TO-ISO.sparql`).
