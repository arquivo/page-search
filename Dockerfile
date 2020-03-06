FROM solr:8.4.1

USER root

COPY searchpages-solr /opt/solr-8.4.1/server/solr/searchpages

RUN chown solr:solr -R /opt/solr-8.4.1/server/solr/searchpages

USER solr

CMD echo "YUPIIII..." && bin/solr start -f -s /opt/solr-8.4.1/server/solr



