package org.arquivo.indexer.parsers;

import com.typesafe.config.Config;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.archive.format.warc.WARCConstants;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCRecord;
import org.archive.io.warc.WARCRecord;
import org.arquivo.indexer.data.PageSearchData;
import org.arquivo.indexer.utils.HTTPHeader;
import org.brotli.dec.BrotliInputStream;
import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.archive.format.warc.WARCConstants.CONTENT_TYPE;
import static org.archive.format.warc.WARCConstants.HEADER_KEY_TYPE;

public class WARCParser {
    private static Log log = LogFactory.getLog(WARCParser.class);

    private List<String> record_type_includes;
    private List<String> record_response_includes;
    private List<String> record_primary_mimetype_includes;
    private List<String> record_mimetype_excludes;

    public WARCParser(Config conf) {
        log.info("Init WARC Parser...");
        this.record_type_includes = conf.getStringList("warc.index.extract.record_type_include");
        this.record_response_includes = conf.getStringList("warc.index.extract.record_response_include");
        this.record_primary_mimetype_includes = conf.getStringList("warc.index.extract.record_primary_mimetype_include");
        this.record_mimetype_excludes = conf.getStringList("warc.index.extract.record_mimetype_exclude");
    }

    private boolean checkMimeType(String mimeType) {

        String[] decomposedMimetype = mimeType.split("/");
        String primaryMimeType = decomposedMimetype[0];
        String subMimeType = decomposedMimetype[1].split(";")[0];

        for (String allowedMimeType : record_primary_mimetype_includes) {
            if (primaryMimeType.equalsIgnoreCase(allowedMimeType)) {
                for (String notAllowedSubMimeType : record_mimetype_excludes) {
                    // exclude some mimetypes
                    if (notAllowedSubMimeType.equalsIgnoreCase(subMimeType)) {
                        return false;
                    }
                }
                return true;
            }
        }
        log.info("Skipping record response with mime type: " + mimeType);
        return false;
    }

    private boolean checkRecordResponseStatus(String statuscode) {
        for (String includedStatusCode : record_response_includes) {
            if (statuscode.startsWith(includedStatusCode))
                return true;
        }
        log.info("Skipping record response with status code: " + statuscode);
        return false;
    }

    private boolean checkRecordType(String type) {
        if (record_type_includes.contains(type)) {
            return true;
        }
        log.info("Skipping record of type " + type);
        return false;
    }


    private void processEnvelopHeader(ArchiveRecordHeader header, PageSearchData doc) throws NoSuchAlgorithmException {
        String timeStamp = (header.getDate().replaceAll("[^0-9]", ""));
        // Date date = getWaybackDate(waybackDate);
        // TODO change to digest utils?
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        ParsedUrl parsedUrl = ParsedUrl.parseUrl(header.getUrl());
        Canonicalizer.WHATWG.canonicalize(parsedUrl);

        byte[] url_md5digest = md5.digest(parsedUrl.toString().getBytes());
        final String url_md5hex = Base64.encodeBase64String(url_md5digest);

        String id = timeStamp + "/" + url_md5hex;

        doc.setUrl(parsedUrl.toString());
        doc.setHost(parsedUrl.getHost());

        // strip out the schema (http(s)://from url, we dont want this in the site field
        String site = parsedUrl.toString().split("://")[1];
        doc.setSite(site);
        doc.setTstamp(timeStamp);
        doc.setId(id);
    }

    /**
     * Adds selected HTTP headers to the Solr document.
     */
    private void processHTTPHeaders(HTTPHeader httpHeaders, String targetUrl, PageSearchData doc) {
        try {
            // This is a simple test that the status code setting worked:
            int statusCodeInt = Integer.parseInt(httpHeaders.getHttpStatus());
            if (statusCodeInt < 0 || statusCodeInt > 1000)
                throw new Exception("Status code out of range: " + statusCodeInt);
            // Get the other headers:
            for (Header h : httpHeaders) {
                if (h.getName().equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                    String servedType = h.getValue();
                    if (servedType.length() > 200)
                        servedType = servedType.substring(0, 200);
                    doc.setType(servedType);
                }
            }
        } catch (NumberFormatException e) {
            log.error("Exception when parsing status code: " + httpHeaders.getHttpStatus() + ": " + e);
        } catch (Exception e) {
            log.error("Exception when parsing headers: " + e);
        }
    }

    /**
     * If HTTP headers are present in the WARC record, they are extracted and passed on to ${@link #processHTTPHeaders}.
     *
     * @return {@link HTTPHeader} containing extracted HTTP status and headers for the record.
     */

    private HTTPHeader processWARCHTTPHeaders(
            ArchiveRecord record, ArchiveRecordHeader warcHeader, String targetUrl, PageSearchData doc)
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


    public PageSearchData extract(String archiveName, ArchiveRecord record) throws NoSuchAlgorithmException,
            IOException, TikaException, SAXException {
        final ArchiveRecordHeader header = record.getHeader();

        PageSearchData doc = new PageSearchData();
        doc.setWarcName(archiveName);

        doc.setWarcOffset(header.getOffset());

        if (!header.getHeaderFields().isEmpty()) {
            // if WARC-TYPE is a WARC if not, probably is a ARC
            if (header.getHeaderFieldKeys().contains(HEADER_KEY_TYPE)) {
                log.debug("Looking at " + header.getHeaderValue(HEADER_KEY_TYPE));

                if (!checkRecordType((String) header.getHeaderValue(HEADER_KEY_TYPE))) {
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
            if (record instanceof WARCRecord) {
                httpHeader = this.processWARCHTTPHeaders(record, header, targetUrl, doc);
            } else if (record instanceof ARCRecord) {
                ARCRecord arcRecord = (ARCRecord) record;
                httpHeader = new HTTPHeader();
                httpHeader.setHttpStatus(Integer.toString(arcRecord.getStatusCode()));
                httpHeader.addAll(arcRecord.getHttpHeaders());

                this.processHTTPHeaders(httpHeader, targetUrl, doc);
                arcRecord.skipHttpHeader();
            } else {
                log.error("FAIL! Unsupported archive record type " + record.getClass().getCanonicalName());
            }
        }
        // VERIFY IF WE WANT TO DISCARD THIS RECORD
        if (!checkRecordResponseStatus(httpHeader.getHttpStatus())) {
            return null;
        }

        if (!checkMimeType(httpHeader.getHeader(CONTENT_TYPE, "DISCARD"))) {
            return null;
        }

        // PAYLOAD HANDLING
        doc.setContentLength(record.available());

        // Calculate DIGEST
        // Should we use HashedCachedInputStream since ArchiveRecord doesn't support mark/reset stream operations

        // TODO use other type of hashing It should be SHA1 right???
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        // I am wrapping it in a DigestInputStream so it calculates the digest in the end
        // TODO Confirm if the digest is being well calculated
        DigestInputStream digestInputStream = new DigestInputStream(record, md5);

        Parser parser = new AutoDetectParser();
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        // if Content-Encoding is brotli, unwrapped before sending to Tika since Tika is not being able to handle this content
        if (httpHeader.getHeader("Content-Encoding", "").equalsIgnoreCase("br")) {
            BrotliInputStream brotliRecord = new BrotliInputStream(digestInputStream);
            parser.parse(brotliRecord, handler, metadata, context);
        } else {
            parser.parse(digestInputStream, handler, metadata, context);
        }

        if (metadata.get("title") == null) {
            doc.setTitle("");
        } else {
            doc.setTitle(metadata.get("title"));
        }

        String encoding = metadata.get("Content-Encoding") != null ? metadata.get("Content-Encoding") : "";
        doc.setEncoding(encoding);
        doc.setTikaContentType(metadata.get("Content-Type"));
        doc.setContent(removeJunkCharacters(handler.toString()));

        HexBinaryAdapter hexBinaryAdapter = new HexBinaryAdapter();
        String digest = hexBinaryAdapter.marshal(digestInputStream.getMessageDigest().digest());
        doc.setDigest(digest);

        return doc;
    }

    public String removeJunkCharacters(String str) {
        Pattern pattern = Pattern.compile("\\s+");
        Matcher matcher = pattern.matcher(str.trim().replaceAll("[\\n\\t]", " "));
        return matcher.replaceAll(" ");
    }
}
