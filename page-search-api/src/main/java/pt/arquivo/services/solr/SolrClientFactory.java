package pt.arquivo.services.solr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

/**
 * Builds the SolrClient used across the app, choosing standalone (HttpSolrClient) vs SolrCloud
 * (CloudSolrClient) based solely on whether a ZK ensemble is configured, so SolrSearchService and
 * PageSearchApplication's shared solrClient bean don't each reimplement this branching.
 */
public final class SolrClientFactory {

    private SolrClientFactory() {
    }

    /** Cloud mode is implied purely by zkHosts being set, there is no separate mode flag. */
    public static boolean isCloudMode(String zkHosts) {
        return zkHosts != null && !zkHosts.trim().isEmpty();
    }

    public static SolrClient buildClient(String baseSolrUrl, String zkHosts, String defaultCollection,
            int connectionTimeoutMillis, int socketTimeoutMillis) {
        if (isCloudMode(zkHosts)) {
            if (defaultCollection == null || defaultCollection.trim().isEmpty()) {
                throw new IllegalStateException(
                        "searchpages.textsearch.service.bean.solr.zkhosts is set but "
                        + "searchpages.textsearch.service.bean.solr.collection is not; a default "
                        + "collection is required in SolrCloud mode.");
            }
            ZkConnectString zk = ZkConnectString.parse(zkHosts);
            if (zk.hosts.isEmpty()) {
                throw new IllegalStateException(
                        "searchpages.textsearch.service.bean.solr.zkhosts (\"" + zkHosts
                        + "\") didn't yield any ZK host:port entries.");
            }
            CloudSolrClient client = new CloudSolrClient.Builder(zk.hosts, zk.chroot)
                    .withConnectionTimeout(connectionTimeoutMillis)
                    .withSocketTimeout(socketTimeoutMillis)
                    .build();
            client.setDefaultCollection(defaultCollection.trim());
            return client;
        }
        return new HttpSolrClient.Builder(baseSolrUrl)
                .withConnectionTimeout(connectionTimeoutMillis)
                .withSocketTimeout(socketTimeoutMillis)
                .build();
    }

    /** Parses the standard ZK connect-string convention: "host1:port,host2:port[/chroot]". */
    static final class ZkConnectString {
        final List<String> hosts;
        final Optional<String> chroot;

        private ZkConnectString(List<String> hosts, Optional<String> chroot) {
            this.hosts = hosts;
            this.chroot = chroot;
        }

        static ZkConnectString parse(String raw) {
            String trimmed = raw.trim();
            int slash = trimmed.indexOf('/');
            String hostsPart = slash >= 0 ? trimmed.substring(0, slash) : trimmed;
            // The chroot keeps its leading '/', which is what CloudSolrClient.Builder expects (e.g. "/solr")
            Optional<String> chroot = slash >= 0 ? Optional.of(trimmed.substring(slash)) : Optional.empty();

            List<String> hosts = new ArrayList<>();
            for (String host : hostsPart.split(",")) {
                String trimmedHost = host.trim();
                if (!trimmedHost.isEmpty()) {
                    hosts.add(trimmedHost);
                }
            }
            return new ZkConnectString(hosts, chroot);
        }
    }
}
