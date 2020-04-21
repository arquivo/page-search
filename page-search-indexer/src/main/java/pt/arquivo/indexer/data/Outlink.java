package pt.arquivo.indexer.data;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Outlink implements Writable {

    private String toUrl;
    private String anchor;

    public Outlink() {
    }

    public Outlink(String toUrl, String anchor) {
        this.toUrl = toUrl;
        if (anchor == null) anchor = "";
        this.anchor = anchor;
    }

    public void readFields(DataInput in) throws IOException {
        toUrl = Text.readString(in);
        anchor = Text.readString(in);
    }

    public void write(DataOutput out) throws IOException {
        Text.writeString(out, toUrl);
        Text.writeString(out, anchor);
    }

    public static Outlink read(DataInput in) throws IOException {
        Outlink outlink = new Outlink();
        outlink.readFields(in);
        return outlink;
    }

    public String getToUrl() {
        return toUrl;
    }

    public String getAnchor() {
        return anchor;
    }


    public boolean equals(Object o) {
        if (!(o instanceof Outlink))
            return false;
        Outlink other = (Outlink) o;
        return this.toUrl.equals(other.toUrl) &&
                this.anchor.equals(other.anchor);
    }

    public String toString() {
        return "toUrl: " + toUrl + " anchor: " + anchor;  // removed "\n". toString, not printLine... WD.
    }
}