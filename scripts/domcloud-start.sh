#!/usr/bin/env sh
# ---------------------------------------------------------------------------
# myITS Broadcast — DOMCloud / Passenger start script
#
# Passenger runs this script from the application root and supplies $PORT.
# The script:
#   1. loads .env.jsp (created by the DOMCloud deployment commands),
#   2. downloads Apache Tomcat 10.1 into the user's home directory if needed,
#   3. writes a minimal server.xml bound to $PORT,
#   4. deploys target/myitsbroadcast.war as the ROOT context,
#   5. starts Tomcat in the foreground so Passenger can supervise it.
#
# No secret is ever written into this repository. PostgreSQL and mail
# credentials must arrive through environment variables or the server-side
# .env.jsp file.
# ---------------------------------------------------------------------------
set -eu

APP_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$APP_DIR"

if [ -f "$APP_DIR/.env.jsp" ]; then
    set -a
    # shellcheck disable=SC1091
    . "$APP_DIR/.env.jsp"
    set +a
fi

PORT="${PORT:-8080}"
TOMCAT_VERSION="${TOMCAT_VERSION:-10.1.34}"
TOMCAT_HOME="${TOMCAT_HOME:-$HOME/.local/apache-tomcat-$TOMCAT_VERSION}"
CATALINA_BASE="${CATALINA_BASE:-$HOME/myitsbroadcast-tomcat}"
APP_STORAGE_DIR="${APP_STORAGE_DIR:-$HOME/myitsbroadcast-storage}"
WAR="$APP_DIR/target/myitsbroadcast.war"

case "$PORT" in
    ''|*[!0-9]*)
        echo "PORT must be a number, got: $PORT" >&2
        exit 1
        ;;
esac

if [ "${APP_ENV:-development}" = "production" ]; then
    : "${DB_URL:?DB_URL must be set in production}"
    : "${DB_USER:?DB_USER must be set in production}"
    : "${DB_PASSWORD:?DB_PASSWORD must be set in production}"
    : "${APP_BASE_URL:?APP_BASE_URL must be set in production}"
fi

if [ ! -f "$WAR" ]; then
    echo "WAR not found at $WAR" >&2
    echo "Build it first with: mvn -q -DskipTests package" >&2
    exit 1
fi

# Tomcat is not preinstalled on DOMCloud; install it once per account.
if [ ! -x "$TOMCAT_HOME/bin/catalina.sh" ]; then
    echo "Installing Apache Tomcat $TOMCAT_VERSION into $TOMCAT_HOME ..."
    mkdir -p "$HOME/.local"
    ARCHIVE="$HOME/.local/apache-tomcat-$TOMCAT_VERSION.tar.gz"
    URL="https://archive.apache.org/dist/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz"

    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "$URL" -o "$ARCHIVE"
    elif command -v wget >/dev/null 2>&1; then
        wget -qO "$ARCHIVE" "$URL"
    else
        echo "Neither curl nor wget is available; cannot download Tomcat." >&2
        exit 1
    fi

    tar -xzf "$ARCHIVE" -C "$HOME/.local"
    rm -f "$ARCHIVE"
fi

mkdir -p "$APP_STORAGE_DIR" \
         "$CATALINA_BASE/conf" \
         "$CATALINA_BASE/logs" \
         "$CATALINA_BASE/temp" \
         "$CATALINA_BASE/work" \
         "$CATALINA_BASE/webapps"

# Reuse the container's global web.xml and logging defaults.
[ -f "$CATALINA_BASE/conf/web.xml" ] || cp "$TOMCAT_HOME/conf/web.xml" "$CATALINA_BASE/conf/web.xml"
[ -f "$CATALINA_BASE/conf/logging.properties" ] || cp "$TOMCAT_HOME/conf/logging.properties" "$CATALINA_BASE/conf/logging.properties"

# Minimal server: HTTP only, no shutdown port, no AJP. Passenger forwards
# public traffic to this port over IPv4 loopback. BIND_ADDRESS can be
# overridden for a local smoke test, but the default keeps Tomcat off the
# public interface.
cat > "$CATALINA_BASE/conf/server.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<Server port="-1" shutdown="SHUTDOWN">
    <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener"/>
    <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener"/>
    <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener"/>
    <Service name="Catalina">
        <Connector port="$PORT"
                   protocol="HTTP/1.1"
                   address="${BIND_ADDRESS:-127.0.0.1}"
                   connectionTimeout="20000"
                   URIEncoding="UTF-8"
                   maxPostSize="3145728"/>
        <Engine name="Catalina" defaultHost="localhost">
            <Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true">
                <Valve className="org.apache.catalina.valves.RemoteIpValve"
                       remoteIpHeader="x-forwarded-for"
                       protocolHeader="x-forwarded-proto"/>
            </Host>
        </Engine>
    </Service>
</Server>
EOF

# Always deploy the freshly built WAR. The exploded directory contains only
# generated Tomcat output; uploads live in APP_STORAGE_DIR, not here.
rm -rf "$CATALINA_BASE/webapps/ROOT"
cp "$WAR" "$CATALINA_BASE/webapps/ROOT.war"

export CATALINA_HOME="$TOMCAT_HOME"
export CATALINA_BASE
export APP_STORAGE_DIR
export CATALINA_OPTS="-Xms64m -Xmx256m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Jakarta ${CATALINA_OPTS:-}"
export JAVA_OPTS="-Dfile.encoding=UTF-8 ${JAVA_OPTS:-}"

echo "Starting myITS Broadcast on port $PORT (CATALINA_BASE=$CATALINA_BASE)"
exec "$TOMCAT_HOME/bin/catalina.sh" run
