FROM solr:8.4.1

USER root

COPY searchpages-solr /opt/solr-8.4.1/server/solr/pagesearch

RUN chown solr:solr -R /opt/solr-8.4.1/server/solr/pagesearch

USER solr

CMD echo "YUPIIII..." && bin/solr start -f -s /opt/solr-8.4.1/server/solr



