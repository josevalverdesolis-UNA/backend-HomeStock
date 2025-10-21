#!/bin/sh
# Lightweight entrypoint to start the Spring Boot app and, if needed, derive
# SPRING_DATASOURCE_* from DATABASE_URL provided by Render Postgres add-on.

# Fail fast on unset variables and errors (no pipefail in /bin/sh)
set -eu

# If DATABASE_URL is provided and explicit SPRING_DATASOURCE_URL is not,
# convert it to the expected Spring variables.
# Example DATABASE_URL: postgres://user:pass@host:5432/dbname?sslmode=require
DATABASE_URL_VAL="${DATABASE_URL:-}"
SPRING_URL_VAL="${SPRING_DATASOURCE_URL:-}"
if [ -n "$DATABASE_URL_VAL" ] && [ -z "$SPRING_URL_VAL" ]; then
  proto_removed=${DATABASE_URL_VAL#*://}
  creds=${proto_removed%@*}
  hostpath=${proto_removed#*@}
  user=${creds%%:*}
  pass=${creds#*:}
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${hostpath}"
  export SPRING_DATASOURCE_USERNAME="${user}"
  export SPRING_DATASOURCE_PASSWORD="${pass}"
fi

# Default port 8080 locally; Render injects PORT at runtime.
PORT_VAL="${PORT:-8080}"

# JVM options can be passed via JAVA_OPTS env var
JAVA_OPTS_VAL="${JAVA_OPTS:-}"

exec java $JAVA_OPTS_VAL -jar app.jar --server.port=${PORT_VAL}

