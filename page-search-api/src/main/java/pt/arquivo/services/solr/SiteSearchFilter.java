package pt.arquivo.services.solr;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.util.ClientUtils;

import pt.arquivo.utils.URLNormalizers;

/**
 * A single siteSearch value, turned into the SURT it restricts results to. The same rule is used for the Solr filter
 * query (against the surts field) and for picking the matching captures out of a document's urlTimestamp values, so
 * both always agree on what a site matches.
 * <ul>
 * <li>A host (sapo.pt, www.sapo.pt, https://www.sapo.pt) matches that host only, see
 * https://github.com/arquivo/pwa-technologies/issues/1014. The protocol and www. are ignored, as they are when
 * indexing.</li>
 * <li>A host prefixed with *. (*.sapo.pt) matches the host and all of its subdomains.</li>
 * <li>A URL with a path (https://www.sapo.pt/some/path) matches every URL that starts with it.</li>
 * </ul>
 * The root of a host is indexed without the closing parenthesis, e.g. (pt,sapo, while its other pages are indexed as
 * (pt,sapo,)/some/path, so a host has to match both.
 */
final class SiteSearchFilter {

    private static final String SUBDOMAINS_WILDCARD = "*.";

    private enum Scope { HOST, SUBDOMAINS, URL_PREFIX }

    private final Scope scope;
    private final String surt;

    private SiteSearchFilter(Scope scope, String surt) {
        this.scope = scope;
        this.surt = surt;
    }

    /**
     * Parses a siteSearch value. Returns null for a blank value, which restricts nothing.
     */
    static SiteSearchFilter parse(String site) {
        String value = site == null ? "" : site.trim();
        boolean subdomains = value.startsWith(SUBDOMAINS_WILDCARD);
        if (subdomains) {
            value = value.substring(SUBDOMAINS_WILDCARD.length());
        }
        if (value.isEmpty()) {
            return null;
        }

        String fullSurt = URLNormalizers.canocalizeSurtUrl(value);
        int hostEnd = fullSurt.indexOf(')');
        if (hostEnd >= 0 && hostEnd < fullSurt.length() - 1) {
            // The wildcard only applies to hosts, a URL with a path is always a prefix of that URL
            return new SiteSearchFilter(Scope.URL_PREFIX, fullSurt);
        }
        String hostSurt = hostEnd >= 0 ? fullSurt.substring(0, hostEnd) : fullSurt;
        return new SiteSearchFilter(subdomains ? Scope.SUBDOMAINS : Scope.HOST, hostSurt);
    }

    /**
     * Parses every siteSearch value, skipping the blank ones.
     */
    static List<SiteSearchFilter> parseAll(String[] sites) {
        List<SiteSearchFilter> filters = new ArrayList<>();
        for (String site : sites) {
            SiteSearchFilter filter = parse(site);
            if (filter != null) {
                filters.add(filter);
            }
        }
        return filters;
    }

    /**
     * The Solr filter query matching any of the given filters, against the surts field.
     */
    static String toFilterQuery(List<SiteSearchFilter> filters) {
        return filters.stream().map(SiteSearchFilter::toSolrClause).collect(Collectors.joining(" OR "));
    }

    /**
     * Whether a SURT (e.g. (pt,sapo,)/some/path) matches any of the given filters.
     */
    static boolean anyMatches(List<SiteSearchFilter> filters, String candidateSurt) {
        return filters.stream().anyMatch(filter -> filter.matches(candidateSurt));
    }

    String toSolrClause() {
        String escapedSurt = ClientUtils.escapeQueryChars(surt);
        switch (scope) {
            case HOST:
                return "surts:" + escapedSurt + " OR surts:" + ClientUtils.escapeQueryChars(surt + ")") + "*";
            default:
                return "surts:" + escapedSurt + "*";
        }
    }

    boolean matches(String candidateSurt) {
        switch (scope) {
            case HOST:
                return candidateSurt.equals(surt) || candidateSurt.startsWith(surt + ")");
            default:
                return candidateSurt.startsWith(surt);
        }
    }
}
