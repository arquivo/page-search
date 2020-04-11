package org.arquivo.indexer.data;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

public class WarcsPathFilter implements PathFilter {

    @Override
    public boolean accept(Path path) {
        String[] fileNameSplits = path.getName().split(".");
        String extension = fileNameSplits[fileNameSplits.length - 1];
        if (extension.equalsIgnoreCase("gz")){
            return true;
        }
        else {
            return false;
        }
    }
}
