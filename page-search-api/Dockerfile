# mvn clean package spring-boot:repackage
FROM openjdk:8-jre-alpine

COPY target/page-search-api-*-SNAPSHOT.jar page-search-api.jar

CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=default", "page-search-api.jar"]

