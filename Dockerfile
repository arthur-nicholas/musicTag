FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-25-jdk -y

COPY . .

RUN apt-get install maven -y
RUN mvn clean install

FROM eclipse-temurin:25

RUN apt-get update && apt-get install -y opustags && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

COPY --from=build target/musicTag-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
