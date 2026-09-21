FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-25-jdk -y
RUN apt-get install -y opustags && rm -rf /var/lib/apt/lists/*
RUN apt-clean

COPY . .

RUN apt-get install maven -y
RUN mvn clean install

FROM eclipse-temurin:25

EXPOSE 8080

COPY --from=build target/musicTag-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
