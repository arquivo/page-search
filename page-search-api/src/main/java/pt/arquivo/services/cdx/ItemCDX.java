package pt.arquivo.services.cdx;

public class ItemCDX {

    private String url;
    private String timestamp;
    private String digest;
    private String mime;
    private String status;
    private String filename;
    private String length;
    private String offset;
    private String collection;

    public ItemCDX(String url, String timestamp, String digest, String mime, String status, String filename,
                   String length, String offset, String collection) {
        this.setUrl(url);
        this.setTimestamp(timestamp);
        this.setDigest(digest);
        this.setMime(mime);
        this.setStatus(status);
        this.setFilename(filename);
        this.setLength(length);
        this.setOffset(offset);
        this.setCollection(collection);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    @Override
    public String toString() {
        return "ItemCDX [url=" + url + ", timestamp=" + timestamp + ", digest=" + digest + ", mime=" + mime
                + ", statusCode=" + status + ", filename=" + filename + ", length=" + length + ", offset=" + offset
                + "]";
    }

    public Boolean checkFields() {
        if (isDefined(url) && isDefined(timestamp) && isDefined(digest) && isDefined(mime)
                && isDefined(status) && isDefined(filename) && isDefined(length) && isDefined(offset))
            return true;
        else
            return false;
    }

    private static boolean isDefined(String str) {
        return str == null ? false : "".equals(str) ? false : true;
    }

    @Override
    public int hashCode() {
        return this.digest.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ItemCDX)) {
            return false;
        }
        ItemCDX other = (ItemCDX) obj;
        return this.digest.equals(other.digest) && this.getTimestamp().equals(other.getTimestamp());
    }

}
