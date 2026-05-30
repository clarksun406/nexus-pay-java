FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /app

COPY pom.xml .
COPY nexuspay-common/pom.xml nexuspay-common/
COPY nexuspay-domain/pom.xml nexuspay-domain/
COPY nexuspay-repository/pom.xml nexuspay-repository/
COPY nexuspay-service/pom.xml nexuspay-service/
COPY nexuspay-web/pom.xml nexuspay-web/

COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/nexuspay-web/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
