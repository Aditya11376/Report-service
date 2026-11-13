# Stage 1: Build JAR
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build project
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime Image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install font libraries for Java AWT Graphics2D EEG waveform rendering
RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig \
    libfreetype6 \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# Non-root user for cloud security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8082
EXPOSE 8082

ENTRYPOINT ["java", "-Djava.awt.headless=true", "-XX:+UseSerialGC", "-Xms128m", "-Xmx350m", "-XX:+TieredCompilation", "-XX:TieredStopAtLevel=1", "-Dspring.jmx.enabled=false", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
