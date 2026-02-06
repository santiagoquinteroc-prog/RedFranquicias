FROM gradle:8-jdk17 AS build

WORKDIR /app

COPY gradle/ ./gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x ./gradlew

COPY src/ ./src/

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

