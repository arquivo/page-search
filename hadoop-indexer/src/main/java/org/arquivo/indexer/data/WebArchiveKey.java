package org.arquivo.indexer.data;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class WebArchiveKey implements WritableComparable<WebArchiveKey> {

    private Text url;
    private LongWritable timeStamp;

    public WebArchiveKey(){
        set(new Text(), new LongWritable());
    }

    public void set(Text url, LongWritable timeStamp){
        this.url = url;
        this.timeStamp = timeStamp;
    }

    public WebArchiveKey(Text url, LongWritable timeStamp){
        this.url = url;
        this.timeStamp = timeStamp;
    }

    public WebArchiveKey(String url, Long timeStamp){
        set(new Text(url), new LongWritable(timeStamp));
    }

    @Override
    public int compareTo(WebArchiveKey key) {
        int cmp = this.url.compareTo(key.getUrl());
        if (cmp != 0){
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

    public LongWritable getTimeStamp() {
        return timeStamp;
    }
}
