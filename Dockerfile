# syntax=docker/dockerfile:1
#
# Runtime-only image. Assumes the fat JAR is already built by Jenkins
# (mvn package) and available under target/ before `docker build` runs.

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user for security
RUN groupadd --system app && useradd --system --gid app --home /app app

# Path to the shaded fat JAR; override at build time if needed:
#   docker build --build-arg JAR_FILE=target/My2048Game-1.0-SNAPSHOT.jar .
ARG JAR_FILE=target/My2048Game-*.jar
COPY ${JAR_FILE} app.jar

# Tomcat embedded listens on 8080 (see com.example.Main)
EXPOSE 8080

# Allow passing extra JVM flags at runtime, e.g. -Ddb.url=...
ENV JAVA_OPTS=""

USER app

# Basic healthcheck against the servlet endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/ || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
