FROM solr:8.4.1

USER root

# COPY pagesearch-solr/solr-db-data /var/solr/data/pagesearch
COPY pagesearch-solr/solr-db-data /opt/solr-8.4.1/server/solr/pagesearch

RUN chown solr:solr -R /opt/solr-8.4.1/server/solr/pagesearch

USER solr

CMD echo "YUPIIII..." && bin/solr start -f -s /opt/solr-8.4.1/server/solr



