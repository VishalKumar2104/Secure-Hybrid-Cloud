# ==============================================================================
# Multi-stage Dockerfile for SERNMS Spring Boot Backend & Enterprise Dashboard
# ==============================================================================

# Stage 1: Build JAR package using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image using lightweight JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-privileged application user
RUN addgroup -S sernms && adduser -S sernms -G sernms

# Copy packaged JAR
COPY --from=builder /build/target/sernms-backend-1.0.0.jar app.jar

# Set ownership
RUN chown -R sernms:sernms /app
USER sernms

# Environment variables
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=mysql \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
