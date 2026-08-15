#!/usr/bin/env bash
#
# EESSI country-code lookup against the EU Publications Office SPARQL endpoint.
# Maps EESSI ISG country codes <-> ISO 3166-1 alpha-2/alpha-3.
#
# Usage:
#   lookup.sh --isg UK          # look up by EESSI ISG code (e.g. UK, EL, NO)
#   lookup.sh --iso GB          # look up by ISO 3166-1 alpha-2 code
#   lookup.sh --all             # dump the full mapping table
#   lookup.sh --isg UK --format json   # raw SPARQL JSON (default: table)
#
# Notes:
#   - EESSI uses UK (ISO GB) and EL (ISO GR), which differ from ISO 3166-1.
#   - Public endpoint, no auth. Requires: curl. Pretty table needs: jq.

set -euo pipefail

ENDPOINT="https://publications.europa.eu/webapi/rdf/sparql"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
QUERY_TEMPLATE="${SCRIPT_DIR}/../query/lookup.rq"

mode=""
code=""
format="table"

die() { echo "error: $*" >&2; exit 1; }

while [[ $# -gt 0 ]]; do
    case "$1" in
        --isg)    mode="isg"; code="${2:-}"; shift 2 ;;
        --iso)    mode="iso"; code="${2:-}"; shift 2 ;;
        --all)    mode="all"; shift ;;
        --format) format="${2:-}"; shift 2 ;;
        -h|--help) sed -n '2,20p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
        *) die "unknown argument: $1" ;;
    esac
done

[[ -n "$mode" ]] || die "specify one of --isg CODE, --iso CODE, or --all"
[[ -f "$QUERY_TEMPLATE" ]] || die "query template not found: $QUERY_TEMPLATE"

# Uppercase the code and guard against SPARQL string injection.
if [[ "$mode" != "all" ]]; then
    [[ -n "$code" ]] || die "--$mode needs a country code"
    code="$(printf '%s' "$code" | tr '[:lower:]' '[:upper:]')"
    [[ "$code" =~ ^[A-Z]{2,3}$ ]] || die "invalid country code: $code (expected 2-3 letters)"
fi

case "$mode" in
    isg) filter="FILTER (str(?isg) = \"$code\")" ;;
    iso) filter="FILTER (str(?iso_alpha2) = \"$code\" || str(?iso_alpha3) = \"$code\")" ;;
    all) filter="" ;;
esac

template="$(cat "$QUERY_TEMPLATE")"
query="${template//'##FILTER_CLAUSE##'/$filter}"

json="$(curl -sS -G "$ENDPOINT" \
    --data-urlencode "query=${query}" \
    --data-urlencode "format=application/sparql-results+json" \
    -H "Accept: application/sparql-results+json")"

if [[ "$format" == "json" ]]; then
    echo "$json"
    exit 0
fi

command -v jq >/dev/null 2>&1 || die "table format needs jq; use --format json instead"

echo "$json" | jq -r '
    ["ISG","ISO2","ISO3","LABEL","URI"],
    (.results.bindings[] | [
        (.isg.value // ""),
        (.iso_alpha2.value // ""),
        (.iso_alpha3.value // ""),
        (.label_en.value // ""),
        (.country_uri.value // "")
    ]) | @tsv' | column -t -s $'\t'
