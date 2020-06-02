package pt.arquivo.indexer.data;

import com.google.gson.annotations.SerializedName;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PageData implements Writable {


    String id = "";
    String title = "";
    String anchor = "";
    String collection = "";

    String content = "";
    int contentLength = 0;
    String digest = "";

    String tstamp = "";

    String host = "";
    String site = "";
    String url = "";
    String surt_url = "";

    @SerializedName("inlinks")
    int nInLinks = 0;
    @SerializedName("outlinks")
    int nOutLinks = 0;

    transient Outlink[] outLinks = new Outlink[0];
    transient Inlinks inLinks = new Inlinks();

    String type = "";
    @SerializedName("primary_type")
    String primaryType = "";
    @SerializedName("sub_type")
    String subType = "";
    @SerializedName("tika_content_type")
    String tikaContentType = "";
    String encoding = "";

    @SerializedName("warc_name")
    String warcName = "";
    @SerializedName("warc_offset")
    long warcOffset = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getTstamp() {
        return tstamp;
    }

    public void setTstamp(String tstamp) {
        this.tstamp = tstamp;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String sit) {
        this.site = sit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String contentType) {
        String type = new String(contentType.toString().split(";")[0]);
        this.type = type;

        String[] contentTypeParts = type.toString().split("/");
        this.primaryType = new String(contentTypeParts[0]);
        this.subType = new String(contentTypeParts[1]);
    }

    public String getPrimaryType() {
        return primaryType;
    }

    public String getSubType() {
        return subType;
    }

    public String getTikaContentType() {
        return tikaContentType;
    }

    public void setTikaContentType(String tikaContentType) {
        this.tikaContentType = tikaContentType;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getWarcName() {
        return warcName;
    }

    public void setWarcName(String warcName) {
        this.warcName = warcName;
    }

    public long getWarcOffset() {
        return warcOffset;
    }

    public void setWarcOffset(long warcOffset) {
        this.warcOffset = warcOffset;
    }

    public void setnInLinks(int nInLinks) {
        this.nInLinks = nInLinks;
    }

    public int getnOutLinks() {
        return nOutLinks;
    }

    public void setnOutLinks(int nOutLinks) {
        this.nOutLinks = nOutLinks;
    }

    public Outlink[] getOutLinks() {
        return outLinks;
    }

    public void setOutLinks(Outlink[] outLinks) {
        this.outLinks = outLinks;
    }

    public Inlinks getInLinks() {
        return inLinks;
    }

    public void setInLinks(Inlinks inLinks) {
        this.inLinks = inLinks;
    }

    public String getSurt_url() {
        return surt_url;
    }

    public void setSurt_url(String surt_url) {
        this.surt_url = surt_url;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        // TODO refactor this mess of duplicated code
        Text.writeString(out, id);
        Text.writeString(out, title);
        Text.writeString(out, anchor);
        Text.writeString(out, content);
        Text.writeString(out, String.valueOf(contentLength));
        Text.writeString(out, digest);
        Text.writeString(out, tstamp);
        Text.writeString(out, host);
        Text.writeString(out, site);
        Text.writeString(out, url);
        Text.writeString(out, String.valueOf(nInLinks));
        Text.writeString(out, String.valueOf(nOutLinks));
        for (int i = 0; i < nOutLinks; i++) {
            outLinks[i].write(out);
        }
        inLinks.write(out);
        Text.writeString(out, type);
        Text.writeString(out, primaryType);
        Text.writeString(out, subType);
        Text.writeString(out, tikaContentType);
        Text.writeString(out, encoding);
        Text.writeString(out, warcName);
        Text.writeString(out, String.valueOf(warcOffset));
        Text.writeString(out, String.valueOf(surt_url));
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.id = Text.readString(in);
        this.title = Text.readString(in);
        this.anchor = Text.readString(in);
        this.content = Text.readString(in);
        this.contentLength = Integer.parseInt(Text.readString(in));
        this.digest = Text.readString(in);
        this.tstamp = Text.readString(in);
        this.host = Text.readString(in);
        this.site = Text.readString(in);
        this.url = Text.readString(in);
        this.nInLinks = Integer.parseInt(Text.readString(in));
        this.nOutLinks = Integer.parseInt(Text.readString(in));

        Outlink[] outlinks = new Outlink[nOutLinks];
        for (int i = 0; i < nOutLinks; i++) {
            Outlink oul = new Outlink();
            oul.readFields(in);
            outlinks[i] = oul;
        }
        this.outLinks = outlinks;

        Inlinks inl = new Inlinks();
        inl.readFields(in);
        this.inLinks = inl;

        this.type = Text.readString(in);
        this.primaryType = Text.readString(in);
        this.subType = Text.readString(in);
        this.tikaContentType = Text.readString(in);
        this.encoding = Text.readString(in);
        this.warcName = Text.readString(in);
        this.warcOffset = Integer.parseInt(Text.readString(in));
        this.surt_url = Text.readString(in);
    }
}
