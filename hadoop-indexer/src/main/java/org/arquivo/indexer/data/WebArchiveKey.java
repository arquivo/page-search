package org.arquivo.indexer.data;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class WebArchiveKey implements WritableComparable<WebArchiveKey> {

    private Text url;
    private Text timeStamp;

    public WebArchiveKey() {
        set(new Text(), new Text());
    }

    public void set(Text url, Text timeStamp) {
        this.url = url;
        this.timeStamp = timeStamp;
    }

    public WebArchiveKey(Text url, Text timeStamp) {
        this.url = url;
        this.timeStamp = timeStamp;
    }

    public WebArchiveKey(String url, String timeStamp) {
        set(new Text(url), new Text(timeStamp));
    }

    @Override
    public int compareTo(WebArchiveKey key) {
        int cmp = this.url.compareTo(key.getUrl());
        if (cmp != 0) {
            return cmp;
        }
        return this.timeStamp.compareTo(key.getTimeStamp());
    }

    @Override
    public void write(DataOutput out) throws IOException {
        url.write(out);
        timeStamp.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.url.readFields(in);
        this.timeStamp.readFields(in);
    }

    public Text getUrl() {
        return url;
    }

    public Text getTimeStamp() {
        return timeStamp;
    }

    public static int keyTimeSlice(String graphTimeSlicePolicy) throws IllegalArgumentException{
        int graphTimeSlice;
        switch (graphTimeSlicePolicy) {
            case "none":
                graphTimeSlice = 0;
                break;
            case "year":
                graphTimeSlice = 4;
                break;
            case "montly":
                graphTimeSlice = 6;
                break;
            case "daily":
                graphTimeSlice = 8;
                break;
            default:
                throw new IllegalArgumentException("Invalid GraphTimeSlice option: " + graphTimeSlicePolicy);
        }
        return graphTimeSlice;
    }
}
