FROM gradle:8.5-jdk17 AS build

WORKDIR /app
COPY . .

RUN gradle build -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
