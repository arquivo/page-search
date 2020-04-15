# mvn clean package spring-boot:repackage
FROM openjdk:8-jre-alpine

COPY target/api-*-SNAPSHOT.jar api.jar

CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=default", "/api.jar"]



