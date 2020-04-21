package pt.arquivo.indexer.data;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Inlink implements Writable {

    private String fromUrl;
    private String anchor;

    public Inlink() {}

    public Inlink(String fromUrl, String anchor) {
        this.fromUrl = fromUrl;
        this.anchor = anchor;
    }

    public void readFields(DataInput in) throws IOException {
        fromUrl = Text.readString(in);
        anchor = Text.readString(in);
    }

    /** Skips over one Inlink in the input. */
    public static void skip(DataInput in) throws IOException {
        Text.skip(in);                                // skip fromUrl
        Text.skip(in);                                // skip anchor
    }

    public void write(DataOutput out) throws IOException {
        Text.writeString(out, fromUrl);
        Text.writeString(out, anchor);
    }

    public static Inlink read(DataInput in) throws IOException {
        Inlink inlink = new Inlink();
        inlink.readFields(in);
        return inlink;
    }

    public String getFromUrl() { return fromUrl; }
    public String getAnchor() { return anchor; }

    public boolean equals(Object o) {
        if (!(o instanceof Inlink))
            return false;
        Inlink other = (Inlink)o;
        return
                this.fromUrl.equals(other.fromUrl) &&
                        this.anchor.equals(other.anchor);
    }

    public int hashCode() {
        return fromUrl.hashCode() ^ anchor.hashCode();
    }

    public String toString() {
        return "fromUrl: " + fromUrl + " anchor: " + anchor;
    }
}

