package org.arquivo.parsers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.commons.httpclient.Header;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.archive.format.warc.WARCConstants;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCRecord;
import org.archive.io.warc.WARCRecord;
import org.arquivo.indexer.HTTPHeader;
import org.arquivo.solr.SolrDocumentWrapper;
import org.brotli.dec.BrotliInputStream;
import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import static org.archive.format.warc.WARCConstants.CONTENT_TYPE;
import static org.archive.format.warc.WARCConstants.HEADER_KEY_TYPE;

public class WARCParser {
    private static Log log = LogFactory.getLog(WARCParser.class);

    private List<String> record_type_includes;
    private List<String> record_response_includes;
    private List<String> record_primary_mimetype_includes;
    private List<String> record_mimetype_excludes;

    public WARCParser(Config conf){
        log.info("Init WARC Parser...");
        this.record_type_includes = conf.getStringList("warc.index.extract.record_type_include");
        this.record_response_includes = conf.getStringList("warc.index.extract.record_response_include");
        this.record_primary_mimetype_includes = conf.getStringList("warc.index.extract.record_primary_mimetype_include");
        this.record_mimetype_excludes = conf.getStringList("warc.index.extract.record_mimetype_exclude");
    }

    private boolean checkMimeType(String mimeType){

        String[] decomposedMimetype = mimeType.split("/");
        String primaryMimeType = decomposedMimetype[0];
        String subMimeType = decomposedMimetype[1].split(";")[0];

        for (String allowedMimeType : record_primary_mimetype_includes){
            if (primaryMimeType.equalsIgnoreCase(allowedMimeType)){
                for (String notAllowedSubMimeType : record_mimetype_excludes){
                    // exclude some mimetypes
                    if (notAllowedSubMimeType.equalsIgnoreCase(subMimeType)) {
                        return false;
                    }
                }
                return true;
            }
        }
        log.debug("Skipping record response with mime type: " + mimeType);
        return false;
    }

    private boolean checkRecordResponseStatus(String statuscode){
        for (String includedStatusCode : record_response_includes){
           if (statuscode.startsWith(includedStatusCode))
            return true;
        }
       log.debug("Skipping record response with status code: " + statuscode);
       return false;
    }

    private boolean checkRecordType(String type){
        if (record_type_includes.contains(type)){
            return true;
        }
        log.debug("Skipping record of type " + type);
        return false;
    }

    // /**
    //  * Returns a Java Date object representing the crawled date.
    //  *
    //  * @param timestamp
    //  * @return
    //  */
    // public static Date getWaybackDate( String timestamp ) {
    //     Date date = new Date();
    //     try {
    //         if( timestamp.length() == 12 ) {
    //             date = ArchiveUtils.parse12DigitDate( timestamp );
    //         } else if( timestamp.length() == 14 ) {
    //             date = ArchiveUtils.parse14DigitDate( timestamp );
    //         } else if( timestamp.length() == 16 ) {
    //             date = ArchiveUtils.parse17DigitDate( timestamp + "0" );
    //         } else if( timestamp.length() >= 17 ) {
    //             date = ArchiveUtils.parse17DigitDate( timestamp.substring( 0, 17 ) );
    //         }
    //     } catch( ParseException p ) {
    //         p.printStackTrace();
    //     }
    //     return date;
    // }

    private void processEnvelopHeader(ArchiveRecordHeader header, SolrDocumentWrapper doc) throws NoSuchAlgorithmException {
       String timeStamp = (header.getDate().replaceAll("[^0-9]",""));
       // Date date = getWaybackDate(waybackDate);
       MessageDigest md5 = MessageDigest.getInstance( "MD5" );
       ParsedUrl parsedUrl = ParsedUrl.parseUrl(header.getUrl());
       Canonicalizer.WHATWG.canonicalize(parsedUrl);

       byte[] url_md5digest = md5.digest(parsedUrl.toString().getBytes());
       final String url_md5hex = Base64.encodeBase64String(url_md5digest);

       String id = timeStamp + "/" + url_md5hex;

       doc.setUrl(parsedUrl.toString());
       doc.setHost(parsedUrl.getHost());
       doc.setTimeStamp(timeStamp);
       doc.setId(id);
    }

    /**
     * Adds selected HTTP headers to the Solr document.
     */
    private void processHTTPHeaders(HTTPHeader httpHeaders , String targetUrl, SolrDocumentWrapper doc) {
        try {
            // This is a simple test that the status code setting worked:
            int statusCodeInt = Integer.parseInt( httpHeaders.getHttpStatus() );
            if( statusCodeInt < 0 || statusCodeInt > 1000 )
                throw new Exception( "Status code out of range: " + statusCodeInt );
            // Get the other headers:
            for( Header h : httpHeaders ) {
                if (h.getName().equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                    String servedType = h.getValue();
                    if (servedType.length() > 200)
                        servedType = servedType.substring(0, 200);
                        doc.setContentType(servedType);
                }
                }
        } catch( NumberFormatException e ) {
            log.error( "Exception when parsing status code: " + httpHeaders.getHttpStatus() + ": " + e );
        } catch( Exception e ) {
            log.error( "Exception when parsing headers: " + e );
        }
    }

    /**
    * If HTTP headers are present in the WARC record, they are extracted and passed on to ${@link #processHTTPHeaders}.
    * @return {@link HTTPHeader} containing extracted HTTP status and headers for the record.
    */

    private HTTPHeader processWARCHTTPHeaders(
            ArchiveRecord record, ArchiveRecordHeader warcHeader, String targetUrl, SolrDocumentWrapper doc)
            throws IOException {
        String statusCode;

        // There are not always headers! The code should check first.
        String statusLine = HttpParser.readLine(record, "UTF-8");
        HTTPHeader httpHeaders = new HTTPHeader();

        if (statusLine != null && statusLine.startsWith("HTTP")) {
            String[] firstLine = statusLine.split(" ");
            if (firstLine.length > 1) {
                statusCode = firstLine[1].trim();
                httpHeaders.setHttpStatus(statusCode);

                Header[] headers = HttpParser.parseHeaders(record, "UTF-8");
                httpHeaders.addAll(headers);
                this.processHTTPHeaders(httpHeaders, targetUrl, doc);
            } else {
                log.warn("Could not parse status line: " + statusLine);
            }
        } else {
            log.warn("Invalid status line: "
                    + warcHeader.getHeaderValue(WARCConstants.HEADER_KEY_FILENAME)
                    + "@"
                    + warcHeader.getHeaderValue(WARCConstants.ABSOLUTE_OFFSET_KEY));
        }
        return httpHeaders;
    }


    public SolrDocumentWrapper extract(String archiveName, ArchiveRecord record) throws NoSuchAlgorithmException, IOException, TikaException, SAXException {
        final ArchiveRecordHeader header = record.getHeader();

        SolrDocumentWrapper doc = new SolrDocumentWrapper(archiveName);
        doc.setWarcOffset(String.valueOf(header.getOffset()));

        if ( !header.getHeaderFields().isEmpty()){
            // if WARC-TYPE is a WARC if not, probably is a ARC
            if (header.getHeaderFieldKeys().contains(HEADER_KEY_TYPE)){
                log.debug("Looking at " + header.getHeaderValue(HEADER_KEY_TYPE));

                if(!checkRecordType((String)header.getHeaderValue(HEADER_KEY_TYPE))){
                    return null;
                }
            }
        }

        if (header.getUrl() == null)
            return null;

        // canonalize??
        String targetUrl = header.getUrl();
        processEnvelopHeader(header, doc);

        // Consume record and parse HTTP headers
        HTTPHeader httpHeader = null;
        if (targetUrl.startsWith("http")) {
            if (record instanceof WARCRecord){
                httpHeader = this.processWARCHTTPHeaders(record, header, targetUrl, doc);
            }
            else if (record instanceof ARCRecord){
                ARCRecord arcRecord = (ARCRecord) record;
                httpHeader = new HTTPHeader();
                httpHeader.setHttpStatus(Integer.toString(arcRecord.getStatusCode()));
                httpHeader.addAll(arcRecord.getHttpHeaders());

                this.processHTTPHeaders(httpHeader, targetUrl, doc);
                arcRecord.skipHttpHeader();
            }
            else {
                log.error("FAIL! Unsupported archive record type " + record.getClass().getCanonicalName());
            }
        }
        // VERIFY IF WE WANT TO DISCARD THIS RECORD
        if (!checkRecordResponseStatus(httpHeader.getHttpStatus())){
           return null;
        }

        if (!checkMimeType(httpHeader.getHeader(CONTENT_TYPE, "DISCARD"))){
           return null;
        }

        // PAYLOAD HANDLING
        doc.setContentLength(record.available());

        // Should we use HashedCachedInputStream ?!?!?
        Parser parser = new AutoDetectParser();
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        // if Content-Encoding is brotli unwrapped before sending to Tika since Tika is not being able to handle
        // this content
        if (httpHeader.getHeader("Content-Encoding", "").equalsIgnoreCase("br")){
            BrotliInputStream brotliRecord = new BrotliInputStream(record);
            parser.parse(brotliRecord, handler,metadata, context);
        }
        else {
            parser.parse(record, handler, metadata, context);
        }

        if (metadata.get("title") == null){
            doc.setTitle("");
        }
        else {
            doc.setTitle(metadata.get("title"));
        }

        doc.setEncoding(metadata.get("Content-Encoding"));
        doc.setTikaType(metadata.get("Content-Type"));
        doc.setContent(handler.toString());

        return doc;
    }

    // TODO MAKE TEST AND REMOVE THIS
    public static void main(String[] args) throws IOException {
        Config conf = ConfigFactory.load();
        WARCParser warcParser = new WARCParser(conf);

        //final String SOLR_URL = "http://localhost:8983/solr/searchpages";
        final String SOLR_URL = conf.getString("warc.solr.server");

        SolrClient solrClient = new HttpSolrClient.Builder(SOLR_URL)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();

        // ArchiveReader reader = ArchiveReaderFactory.get("/home/dbicho/IdeaProjects/searchpages/src/main/resources/chunked.gzip.html.record.warc");
        ArchiveReader reader = ArchiveReaderFactory.get("/home/dbicho/IdeaProjects/searchpages/src/main/resources/rec-20200304135501947557-oreas-PCIWT3XR.warc.gz");
        Iterator<ArchiveRecord> ir = reader.iterator();
        int recordCount = 1;
        int lastFailedRecord = 0;

        // Iterate though each record in the WARC file
        while (ir.hasNext()) {
            try {
                ArchiveRecord rec = ir.next();
                try {
                    SolrDocumentWrapper doc = warcParser.extract("teste", rec);

                    // move this code
                    System.out.println(doc.getSolrInputDocument());
                    // solrClient.add(doc.getSolrInputDocument());
                    // solrClient.commit();

                } catch (Exception e) {
                    continue;
                } catch (OutOfMemoryError e) {
                    continue;
                }
            } catch (RuntimeException e) {
                if (lastFailedRecord != recordCount) {
                    lastFailedRecord = recordCount;
                    continue;
                }
                log.error("Failed to reach next record, last record already on error - skipping the rest of the records");
                break;
            }
        }
    }
}
