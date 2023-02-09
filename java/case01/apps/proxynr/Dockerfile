# Download agent
FROM ubuntu:latest AS agent

RUN apt-get update \
  && apt-get install -y wget

RUN mkdir /agent
RUN wget -O /agent/newrelic.jar https://download.newrelic.com/newrelic/java-agent/newrelic-agent/8.0.0/newrelic-agent-8.0.0.jar

# Build
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# Package stage
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/proxy-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar

ADD newrelic/newrelic.yml /usr/local/lib
COPY --from=agent /agent/newrelic.jar /usr/local/lib/newrelic.jar

EXPOSE 8080

ENTRYPOINT ["java","-javaagent:/usr/local/lib/newrelic.jar","-jar","/usr/local/lib/app.jar"]
