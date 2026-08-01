#!/usr/bin/env bash
set -e

if command -v /usr/libexec/java_home &>/dev/null; then
    JAVA="$(/usr/libexec/java_home -v 21)/bin/java"
else
    JAVA="java"
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$JAVA" -jar "$SCRIPT_DIR/build/libs/shuttle-mcp.jar"
