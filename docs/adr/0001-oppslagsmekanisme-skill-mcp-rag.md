# ADR-0001: Mekanisme for agent-oppslag av EESSI-landkoder — skill, MCP eller RAG

**Dato:** 2026-08-15
**Status:** Foreslått
**Beslutningstakere:** tk2-country-code (torsten.kirschner@nav.no)

## Kontekst

`tk2-country-code` mapper EESSI ISG-landkoder til ISO 3166-1 (alpha-2/alpha-3) ved å
spørre EUs Publications Office SPARQL-endepunkt
(`https://publications.europa.eu/webapi/rdf/sparql`). EESSI bruker to koder som avviker
fra ISO: **UK** (ISO `GB`) og **EL** (ISO `GR`).

Vi har allerede to realiseringer:

- **Java-tjeneste** (Spring Boot + RDF4J) for programmatisk oppslag.
- **Skill** `eessi-country-lookup` for agent-oppslag via `curl` mot samme SPARQL.

`MCP-TODO.md` konkluderte tidligere med at en skill holder, og at MCP er overkill inntil
gjenbruk på tvers av klienter blir et reelt behov. Det nye spørsmålet er om en
**RAG-server** er riktig mekanisme for dette oppslaget. Vi må ta en bevisst beslutning
nå for å unngå å bygge unødvendig infrastruktur (vektordatabase, embeddings) rundt et
lite, deterministisk oppslag.

Konsekvensen av å **ikke** beslutte: risiko for at RAG velges på autopilot fordi det er
et populært mønster, med påfølgende driftskostnad og — verre — approksimasjonsfeil i et
oppslag som må være eksakt.

**Antakelser** (oppgitt fordi de ikke er bekreftet av bruker):
- Scope er fortsatt kun ISG↔ISO-tabellen, ikke et bredere EESSI-dokumentkorpus.
- Driveren bak RAG-tanken er uskarpe/flerspråklige landnavn og feilstavinger.
- Primærkonsument er Copilot CLI (én klient).

## Beslutning

**For landkodeoppslaget: behold skillen `eessi-country-lookup` som primær
agent-mekanisme.** RAG forkastes for denne use-casen. MCP holdes som betinget neste steg
kun dersom gjenbruk på tvers av klienter blir reelt (uendret fra `MCP-TODO.md`).

RAG vurderes på nytt **kun** hvis scope utvides fra kodetabellen til et bredere,
ustrukturert EESSI-korpus (dokumenter, evidences, domenemapping — SDG-sporet i
`IAEG.md`). Selv da beholdes deterministisk oppslag for selve kodemappingen; RAG brukes
bare for fritekst-dokumentsøk.

## Alternativer vurdert

| Alternativ | Fordeler | Ulemper | Nav-vurdering |
|-----------|----------|---------|---------------|
| A: Skill (valgt) | Ingen state/drift; gjenbruker kanonisk SPARQL; deterministisk; lav kostnad | Én-klients oppslag; ingen caching/retries | I tråd med essensiell kompleksitet — minst mulig maskineri |
| B: MCP-server | Typet kontrakt; caching/retries; gjenbruk på tvers av klienter | Middels drift; egen tjeneste å vedlikeholde | Berettiget først ved reelt flerklient-behov |
| C: RAG-server | Uskarpe/semantiske søk i store korpus | Approksimasjon → feil land; hallusinasjon; embeddings + vektor-DB; re-indeksering | Feil verktøy for eksakt, strukturert oppslag |
| D: Gjøre ingenting | Null arbeid; dagens skill fungerer | Beslutningen forblir udokumentert; RAG-spørsmålet dukker opp igjen | Ikke akseptabelt — vi trenger sporbar begrunnelse |

### Alternativ A: Skill (valgt)
- **Fordeler:** Oppslaget er én HTTP-forespørsel + parsing mot offentlig data. Ingen
  hemmeligheter, ingen state. Deterministisk og korrekt per definisjon.
- **Ulemper:** Ingen innebygd caching mot et tidvis tregt endepunkt; bundet til én
  klient (Copilot CLI).
- **Nav-vurdering:** Enkleste løsning som løser problemet fullt ut. Følger prinsippet om
  essensiell kompleksitet.

### Alternativ C: RAG-server (forkastet)
- **Fordeler:** Sterk på uskarpe, semantiske spørsmål mot store ustrukturerte
  tekstmengder.
- **Ulemper:** Landkodetabellen er ~30 rader, strukturert og autoritativ. RAG legger til
  embeddings-modell, vektordatabase, re-indeksering ved EU-oppdateringer, og innfører
  approksimasjon: semantisk nærhet kan returnere feil land. Genereringssteget kan
  hallusinere koder. Alt dette forverrer korrektheten for et oppslag som må være 100 %
  eksakt.
- **Nav-vurdering:** Klassisk accidental complexity. Forkastet.

## Nav-spesifikke vurderinger

### Sikkerhet
- **Dataklassifisering:** Åpen. Offentlig EU-autoritetsdata (landkoder og -navn). Ingen
  persondata (PII), ingen fnr.
- **Auth-mekanisme:** Ingen. Endepunktet er åpent HTTP uten autentisering.
- **PII-håndtering:** Ikke relevant — ingen persondata behandles eller logges.
- **Vurdering:** RAG ville ikke endret klassifiseringen, men lagt til en vektordatabase
  som ny angreps- og driftsflate uten sikkerhetsgevinst. Skill holder angrepsflaten
  minimal.

### Plattform
- **Nais-konfigurasjon:** Ingen endring. Skillen kjører klient-side i Copilot CLI; ingen
  ny tjeneste å deploye.
- **Ressursbehov:** Neglisjerbart for skill. RAG ville krevd vedvarende
  vektordatabase + embeddings-compute — reell Nais-kostnad uten gevinst.
- **Observerbarhet:** Ikke nødvendig for et klient-side skill-oppslag. En eventuell
  fremtidig MCP/tjeneste bør ha metrikk for endepunkt-latens og feilrate.

### Team-påvirkning
- **Berørte team:** Kun tk2-country-code. Ingen andre konsumenter identifisert.
- **Migrasjonsstrategi:** Ingen migrering — beslutningen bekrefter status quo (behold
  skill) og dokumenterer hvorfor RAG forkastes.
- **Tilbakerulle-strategi:** Ikke relevant; ingen teknisk endring i tjenesten.

## Konsekvenser

### Positive
- Unngår unødvendig infrastruktur (vektor-DB, embeddings) og tilhørende driftskostnad.
- Bevarer 100 % deterministisk, korrekt oppslag.
- Beslutningen er nå sporbar; RAG-spørsmålet er avklart med begrunnelse.

### Negative
- Ingen caching/retries mot et tidvis tregt EU-endepunkt (kjent begrensning, adresseres
  av MCP hvis/ når det trengs).
- Bundet til én klient inntil MCP eventuelt bygges.

### Risiko
- **Scope-endring:** Hvis EESSI-behovet utvides til fritekst-dokumenter, må denne ADR
  revurderes (RAG for dokumentsøk, deterministisk oppslag for koder beholdes).
- **Endepunkt-ustabilitet:** EU-SPARQL kan være tregt/nede; skillen har ingen fallback.

## Aksjonspunkter

- [ ] Oppdater `MCP-TODO.md` med referanse til denne ADR-en og en kort RAG-seksjon.
- [ ] Bekreft de tre antakelsene (scope, driver, konsument) med bruker; revurder ved
      avvik.
- [ ] (Betinget) Hvis scope utvides til dokumentkorpus: skisser hybrid-arkitektur — RAG
      for dokumenter + deterministisk SPARQL-oppslag for koder.
- [ ] (Betinget) Hvis flerklient-behov oppstår: realiser MCP-verktøyene fra
      `MCP-TODO.md` (`lookup_country_by_isg`, `lookup_country_by_iso`,
      `list_country_mappings`).
