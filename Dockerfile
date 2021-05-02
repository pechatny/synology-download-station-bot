FROM gradle:7.0.0-jdk11 AS builder
COPY ./ /home/gradle/project
WORKDIR /home/gradle/project
RUN gradle build

FROM openjdk:11-jdk-slim
COPY --from=0 /home/gradle/project/build/libs/*.jar app.jar
RUN mkdir /torrents

ENTRYPOINT ["java","-jar","/app.jar"]