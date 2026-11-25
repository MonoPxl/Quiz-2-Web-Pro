#!/usr/bin/env sh
# ---------------------------------------------------------------------------
# myITS Broadcast — local Tomcat runner (Linux, macOS, Git Bash)
#
# Builds the WAR and starts Tomcat on PORT (default 8080) using the same
# start script as DOMCloud. Configuration is read from .env.jsp, so create it
# from .env.jsp.example first. The legacy Laravel app keeps its own .env.
#
# Windows PowerShell users can run the equivalent commands from README.md.
# ---------------------------------------------------------------------------
set -eu

APP_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$APP_DIR"

if [ -f "$APP_DIR/.env.jsp" ]; then
    set -a
    # shellcheck disable=SC1091
    . "$APP_DIR/.env.jsp"
    set +a
else
    echo "No .env.jsp found. Copy .env.jsp.example to .env.jsp and fill in the PostgreSQL settings." >&2
    exit 1
fi

mvn -q -DskipTests package

export PORT="${PORT:-8080}"
export CATALINA_BASE="${CATALINA_BASE:-$APP_DIR/storage-jsp/tomcat}"
export TOMCAT_HOME="${TOMCAT_HOME:-$HOME/.local/apache-tomcat-${TOMCAT_VERSION:-10.1.34}}"

exec "$APP_DIR/scripts/domcloud-start.sh"
