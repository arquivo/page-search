# This Dockerfile was used to build the latest solr image for arquivo/pagesearch-solr-test. 

FROM openjdk:11
# Using openjdk image instead of solr image because solr image uses a more recent jdk version which doesn't include Nashorn. 
# It's easier to install Solr with older jdk that includes Nashorn than use Solr image and install Nashorn.

USER root

# Installing Solr
WORKDIR /opt/
RUN wget 'https://www.apache.org/dyn/closer.lua/solr/solr/9.6.1/solr-9.6.1.tgz?action=download' -O solr-9.6.1.tgz
RUN tar -xvf solr-9.6.1.tgz
RUN rm solr-9.6.1.tgz

# Creating Solr core. Made the text_index_v6.tar.gz manually. 
WORKDIR /opt/solr-9.6.1/server/solr/configsets
COPY test-data/text_index_v6.tar.gz text_index_v6.tar.gz
RUN tar -xzvf text_index_v6.tar.gz
RUN rm text_index_v6.tar.gz

# Adding test data to the core. The data used was the Roteiro collection, 
#     which was posted into a fresh solr instance with the text_index_v6 
#     core configured. 
WORKDIR /opt/solr-9.6.1/server/solr/configsets/text_index_v6/data/
COPY test-data/data.tar.gz data.tar.gz
RUN tar -xzvf data.tar.gz .
RUN rm data.tar.gz

# Creating solr user
RUN groupadd solr
RUN useradd -rm -g root -G sudo,solr -u 1001 solr

# Making sure all needed folders exist with the proper permissions
RUN mkdir /var/solr
RUN mkdir /var/solr/data
RUN chown solr:solr -R /var/solr
RUN chown solr:solr -R /opt/solr-9.6.1/

USER solr
WORKDIR /opt/solr-9.6.1

# Adding scripting module, needed by core
ENV SOLR_MODULES=scripting

# Setting host to 0.0.0.0, otherwise we won't be able to forward requests
ENV SOLR_JETTY_HOST="0.0.0.0"

# YUPIIII
CMD bin/solr start -f -s /opt/solr-9.6.1/server/solr



