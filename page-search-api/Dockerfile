FROM tomcat:latest

COPY target/page-search-api-*-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

COPY search-servers.txt .

ENV NUTCHWAX_SEARCH_FILE=./
