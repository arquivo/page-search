# mvn clean package spring-boot:repackage
FROM openjdk:8-jre-alpine

WORKDIR app

COPY target/api-*-SNAPSHOT.jar api.jar

CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=default", "app/api.jar"]

