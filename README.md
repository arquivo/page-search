# Arquivo.pt Page Search System

This project aims to replace the legacy search system. The legacy search system, Nutchwax based, will be decrepated by Solr as the full-text search backend. In order to accomplish this, it provides a new API implementation to decouple Arquivo.pt API from the old project, making it backend agnostic and working with both Nutchwax and Solr systems.

## Compile Page Search

`page-search-api` uses NutchWaxSearchService as a legacy full-text backend, which depends on
`pt.arquivo:pwalucene` and `org.archive.nutchwax:nutchwax-plugins` (and their own transitive
legacy dependencies). These have never been published to a public Maven repository, so their
jars are vendored in `page-search-api/maven-local-repository` and resolved via a `<repository>`
entry in `page-search-api/pom.xml` — no manual local install step is required.

**NOTE: Do not try to compile with other Java than Java 8**, because of the nutchwax dependecies

```
$ git clone https://github.com/arquivo/pagesearch.git
$ mvn clean install -f pagesearch/pom.xml
```

### Installing Java 8

On Ubuntu, `openjdk-8-jdk` is no longer available in the default repositories.
Install it from the Eclipse Temurin (Adoptium) repository instead:

```
$ sudo apt update && sudo apt install -y wget apt-transport-https gpg
$ wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /usr/share/keyrings/adoptium.gpg
$ echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(awk -F= '/VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
$ sudo apt update
$ sudo apt install -y temurin-8-jdk
```

### Building with Java 8 without changing your system default

If you have other JDKs installed, set `JAVA_HOME` for the build instead of
switching the system-wide default:

```
$ JAVA_HOME=/usr/lib/jvm/temurin-8-jdk-amd64 mvn clean install -f pagesearch/pom.xml
```

Alternatively, switch the default JVM for your whole session with:

```
$ sudo update-alternatives --config java
$ sudo update-alternatives --config javac
```

### Run without integration Tests

To do this just run mvn install with the following profile deactivated:
```
$ mvn clean install -f pagesearch/pom.xml -P !docker-integration-tests
```

### Generate new arquivo/pagesearch-solr-test docker image for the integration tests

Note: you need log with hub.docker.com to be able to publish an image to Arquivo's docker hub repository.

```
$ cd pagesearch/scripts
$ ./build-solr-test-image.sh
```

## Run Page Search API Locally

`page-search-api` is a Spring Boot app packaged as a WAR (deployed onto Tomcat in
production, see `page-search-api/Dockerfile`), but for local development you don't
need to package or deploy it at all — run it directly with the Spring Boot Maven
plugin, which boots an embedded Tomcat:

```
$ JAVA_HOME=/usr/lib/jvm/temurin-8-jdk-amd64 mvn -f page-search-api/pom.xml spring-boot:run
```

By default this listens on port `8081` (see `server.port` in
`page-search-api/src/main/resources/application.properties`).

### Pointing at a Solr backend

`searchpages.textsearch.service.bean` selects the search backend: any value other
than `nutchwax` loads `SolrSearchService` (see
`PageSearchApplication#generateService()`), which in turn only reads
`searchpages.textsearch.service.bean.solr.link` for the Solr base URL — e.g.
`http://<host>:<port>/solr/<collection>`.

To point at a different Solr instance/collection without editing the committed
`application.properties`, pass both properties as Spring Boot run arguments:

```
$ JAVA_HOME=/usr/lib/jvm/temurin-8-jdk-amd64 mvn -f page-search-api/pom.xml spring-boot:run \
    -Dspring-boot.run.arguments="--searchpages.textsearch.service.bean=solr --searchpages.textsearch.service.bean.solr.link=http://<host>:<port>/solr/<collection>"
```

Once running, verify it's serving real results:

```
$ curl "http://localhost:8081/textsearch?q=test&maxItems=1"
```

Note: other deployed environments (dev/preprod/prod) may point at different Solr
hosts/collections than the one committed in `application.properties` — that
per-environment configuration lives in each environment's own deployment setup,
not in this repository.

## Page Search API Architecture 

![](docs/img/PageSearchArchitecture.png)

### Api Documentation

The app exposes its OpenAPI 3 docs under `/textsearch/api-docs` (configured via
`springdoc.api-docs.path`/`springdoc.swagger-ui.path` in
`page-search-api/src/main/resources/application.properties`), matching the
public URL convention proposed in
[pwa-technologies#1588](https://github.com/arquivo/pwa-technologies/issues/1588)
— so an Apache reverse proxy only needs a straight passthrough, no path
rewriting. Relative to wherever the app is mounted, e.g. locally:

- Swagger UI: http://localhost:8081/textsearch/api-docs
- OpenAPI 3 spec (JSON): http://localhost:8081/textsearch/api-docs/v3

Note: how (or whether) these paths are exposed publicly through Apache in
dev/preprod/prod is per-environment reverse-proxy configuration, not part of
this repository.

## Page Search Indexer

### How to index a collection of WARC files 

Available jobs:
* HdfsPageSearchDataDriver
* PageSearchDataDriver
* InvertLinksDriver
* SolrPageDocDriver

Example of expected workflow:
```shell script
$ yarn jar pagesearch-index-job-0.0.1-jar-with-dependencies.jar HdfsPageSearchDataDriver -D collection="TESTE" input.txt output
$ yarn jar pagesearch-index-job-0.0.1-jar-with-dependencies.jar InvertLinksDriver -D mapred.reduce.tasks=<nr_reduces> output
$ yarn jar pagesearch-index-job-0.0.1-jar-with-dependencies.jar SolrPageDocDriver -D mapred.reduce.tasks=<nr_reduces> output
```

### Full Indexing Workflow script:

[https://github.com/arquivo/page-search/blob/master/scripts/index-pagesearch.sh](https://github.com/arquivo/page-search/blob/master/scripts/index-pagesearch.sh)

```shell script
$ ./index-pagesearch.sh <hdfs_warcfiles_folder> <hdfs_output_folder> <collection_name>
```
Example:
```shell script
$ ./index-pagesearch.sh /user/dbicho/AWP2 /user/dbicho/output_AWP2 AWP2
```


### Configuring which records to process and index

Write a reference.conf with the parsing configurations. The default configurations of the parser are:
```
{
    "warc" : {
        "solr":{
            "server": "http://localhost:8983/solr/searchpages"
        },
        "index":{
            "extract":{
                # Restrict record types:
                "record_type_include" : [
                    response, revisit
                ],
                "record_response_include" : [
                    "2"
                ],
                "record_primary_mimetype_include" : [
                    text, application
                ],
                "record_mimetype_exclude" : [
                    xml, css, javascript, x-javascript, json
                ]
            }
        }
    }
}


