# syntax=docker/dockerfile:1.6
#
# Multi-stage build for the Arrive Competitor Intelligence app.
# Stage 1 builds the Vaadin production bundle + Spring Boot jar.
# Stage 2 is a slim JRE image that runs the jar.

# ----- build stage -----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Cache the Gradle wrapper + dependency graph independently of source changes.
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon --version

# Now pull source and produce the production jar (Vaadin frontend included).
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -Pvaadin.productionMode \
    && cp build/libs/*.jar /workspace/app.jar

# ----- runtime stage -----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user — Spring Boot doesn't need root and this limits blast radius.
RUN groupadd --system arrive && useradd --system --gid arrive --home /app arrive
COPY --from=build /workspace/app.jar /app/app.jar
RUN chown -R arrive:arrive /app
USER arrive

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
