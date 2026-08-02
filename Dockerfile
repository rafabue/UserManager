# build em dois estagios, primeiro compila front e back depois carrega o o jar e um JRE
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY backend/pom.xml backend/pom.xml
RUN mvn -f backend/pom.xml -Pfrontend -B dependency:go-offline

COPY backend backend
COPY frontend frontend

# profile frontend baixa o node, compila o vue e embute o bundle no jar
RUN mvn -f backend/pom.xml -Pfrontend -B -DskipTests clean package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /build/backend/target/user-manager-*.jar app.jar

# banco em volume, os dados vao sobreviver a recriacao do container
ENV APP_DB_PATH=/app/data/usermanager
VOLUME /app/data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
