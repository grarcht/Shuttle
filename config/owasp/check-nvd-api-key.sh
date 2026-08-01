#!/usr/bin/env bash
# Checks whether an NVD API key is valid by making a single test request.
#
# Usage:
#   ./config/owasp/check-nvd-api-key.sh <api-key>
#   ./config/owasp/check-nvd-api-key.sh abc12345-...
#
# Exit codes:
#   0 — key is valid (HTTP 200)
#   1 — key is revoked or invalid (HTTP 401/403) — request a new key at:
#         https://nvd.nist.gov/developers/request-an-api-key
#   2 — NVD API is temporarily unavailable (HTTP 503) — not a key problem
#   3 — unexpected HTTP status or no network

set -e

KEY="$1"

if [ -z "$KEY" ]; then
    echo "Error: API key argument required."
    echo "Usage: $0 <api-key>"
    exit 3
fi

STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "apiKey: $KEY" \
    "https://services.nvd.nist.gov/rest/json/cves/2.0?resultsPerPage=1")

case "$STATUS" in
    200)
        echo "OK ($STATUS) — API key is valid."
        exit 0
        ;;
    401|403)
        echo "INVALID ($STATUS) — API key is revoked or was never activated."
        echo "Request a new key at: https://nvd.nist.gov/developers/request-an-api-key"
        echo "Then update the NVD_API_KEY secret in GitHub → Settings → Secrets and variables → Actions."
        exit 1
        ;;
    503)
        echo "UNAVAILABLE ($STATUS) — NVD API is temporarily down. The key may still be valid."
        echo "Try again later or check https://nvd.nist.gov for status."
        exit 2
        ;;
    *)
        echo "UNEXPECTED ($STATUS) — check your network connection or the NVD API endpoint."
        exit 3
        ;;
esac
