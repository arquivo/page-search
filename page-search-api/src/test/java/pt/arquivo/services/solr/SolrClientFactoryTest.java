package pt.arquivo.services.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SolrClientFactoryTest {

    @Test
    public void isCloudMode_falseForNullOrBlank() {
        assertThat(SolrClientFactory.isCloudMode(null)).isFalse();
        assertThat(SolrClientFactory.isCloudMode("")).isFalse();
        assertThat(SolrClientFactory.isCloudMode("   ")).isFalse();
    }

    @Test
    public void isCloudMode_trueWhenSet() {
        assertThat(SolrClientFactory.isCloudMode("zk1:2181")).isTrue();
    }

    @Test
    public void buildClient_standaloneModeReturnsHttpSolrClient() {
        SolrClient client = SolrClientFactory.buildClient(
                "http://solr.example.com/solr/pages", null, null, 5000, 20000);

        assertThat(client).isInstanceOf(HttpSolrClient.class);
    }

    @Test
    public void buildClient_cloudModeReturnsCloudSolrClientWithDefaultCollection() {
        SolrClient client = SolrClientFactory.buildClient(
                "http://unused/solr", "zk1:2181,zk2:2181", "pages", 5000, 20000);

        assertThat(client).isInstanceOf(CloudSolrClient.class);
        assertThat(((CloudSolrClient) client).getDefaultCollection()).isEqualTo("pages");
    }

    @Test
    public void buildClient_cloudModeWithoutCollectionThrows() {
        assertThatThrownBy(() -> SolrClientFactory.buildClient(
                "http://unused/solr", "zk1:2181", null, 5000, 20000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("collection");
    }

    @Test
    public void buildClient_cloudModeWithBlankCollectionThrows() {
        assertThatThrownBy(() -> SolrClientFactory.buildClient(
                "http://unused/solr", "zk1:2181", "   ", 5000, 20000))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void parse_singleHostNoChroot() {
        SolrClientFactory.ZkConnectString result = SolrClientFactory.ZkConnectString.parse("zk1:2181");

        assertThat(result.hosts).containsExactly("zk1:2181");
        assertThat(result.chroot).isEmpty();
    }

    @Test
    public void parse_multipleHostsWithChroot() {
        SolrClientFactory.ZkConnectString result =
                SolrClientFactory.ZkConnectString.parse("zk1:2181,zk2:2181,zk3:2181/solr");

        assertThat(result.hosts).containsExactly("zk1:2181", "zk2:2181", "zk3:2181");
        assertThat(result.chroot).contains("/solr");
    }

    @Test
    public void parse_ignoresWhitespaceAndEmptyTokens() {
        SolrClientFactory.ZkConnectString result =
                SolrClientFactory.ZkConnectString.parse(" zk1:2181 , , zk2:2181 ");

        assertThat(result.hosts).containsExactly("zk1:2181", "zk2:2181");
    }
}
