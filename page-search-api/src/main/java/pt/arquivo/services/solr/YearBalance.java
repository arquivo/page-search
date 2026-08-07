package pt.arquivo.services.solr;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Ranking that doesn't let the thin years of the archive be buried by the dense ones.
 *
 * <p>The archive holds many times more documents from some years than from others, so a query with no date filter
 * fills its first page with whichever years were crawled the most, not with the pages that matter the most on each
 * year. This gives every document a multiplier that grows the thinner its year is:
 *
 * <pre>weight(year) = (documents of the densest year / documents of that year) ^ exponent</pre>
 *
 * <p>How large that exponent may be is set by how close together the scores of a query are. Measured against the
 * index of 96M documents, the 50 best scoring documents of a common query sat between 29.85 and 26.78, a spread of
 * 1.11x, most of them tied. A multiplier of 1.4x on the thinnest year is already enough to fill the whole first page
 * with that year, and normalizing the volumes all the way (a 995x multiplier there) isn't ranking at all, it is
 * sorting by year.
 *
 * <p>So the strength the user asks for, 0 to 1, is spread over the multipliers that are actually usable: 0 leaves the
 * ranking exactly as it is, and 1 lifts the thinnest year of the archive by {@link #MAX_LIFT}, which on that index
 * keeps 8 different years on the first page where a heavier hand leaves 3.
 *
 * <p>The multiplier is sent to Solr as a function over the year field, and it is the same for every query, so it is
 * built out of the cached {@link YearVolumes} without asking Solr anything else.
 */
public final class YearBalance {

    /** What yearBalance=true means, when the user asks for the feature without saying how strongly. */
    public static final double DEFAULT_STRENGTH = 0.5;

    /** Weights are never below this, a year is either lifted or left alone, never pushed down. */
    private static final double NEUTRAL_WEIGHT = 1.0;

    /**
     * How much the thinnest year of the archive is lifted at full strength. Kept just above the spread the scores of
     * a query have among themselves, so that the strongest setting still ranks by relevance inside each year instead
     * of turning the first page into a single year.
     */
    static final double MAX_LIFT = 1.25;

    /**
     * Anything above this is still an epoch value that no year matched, e.g. a document dated outside the archive.
     * Weights are far smaller than this, so they pass through the mapping that neutralizes those.
     */
    private static final long EPOCH_LIKE_FLOOR = 1000000000L;

    private static final long EPOCH_LIKE_CEILING = 100000000000000L;

    private YearBalance() {
    }

    /**
     * The multiplier of each year of the archive, keyed by year.
     *
     * @param volumesPerYear how many documents the archive holds per year
     * @param strength 0 leaves every year at 1, 1 lifts the thinnest year of the archive by {@link #MAX_LIFT}
     * @return the weight per year, empty when there is nothing to tell the years apart
     */
    static Map<String, Double> weights(Map<String, Long> volumesPerYear, double strength) {
        if (strength <= 0 || volumesPerYear.isEmpty()) {
            return Collections.emptyMap();
        }

        long densestYear = volumesPerYear.values().stream().mapToLong(Long::longValue).max().orElse(0);
        long thinnestYear = volumesPerYear.values().stream().mapToLong(Long::longValue).filter(v -> v > 0).min()
                .orElse(0);
        if (densestYear <= 0 || thinnestYear <= 0) {
            return Collections.emptyMap();
        }

        // The years of an archive that are all the same size have nothing to balance
        double widestRatio = (double) densestYear / thinnestYear;
        if (widestRatio <= 1) {
            return Collections.emptyMap();
        }

        // The exponent that lifts the thinnest year of this archive by MAX_LIFT when the strength is at its highest
        double exponent = strength * Math.log(MAX_LIFT) / Math.log(widestRatio);

        Map<String, Double> weights = new LinkedHashMap<>();
        for (Map.Entry<String, Long> yearVolume : volumesPerYear.entrySet()) {
            // A year the archive holds nothing for has nothing to rank, so it is left neutral
            if (yearVolume.getValue() <= 0) {
                weights.put(yearVolume.getKey(), NEUTRAL_WEIGHT);
                continue;
            }
            double ratio = (double) densestYear / yearVolume.getValue();
            weights.put(yearVolume.getKey(), Math.max(Math.pow(ratio, exponent), NEUTRAL_WEIGHT));
        }
        return weights;
    }

    /**
     * The multiplier as a Solr function query: the year field is turned into milliseconds and then mapped, one year
     * at a time, into the weight of that year. A document whose year isn't in the table (dated outside the archive,
     * or without a date at all) comes out neutral, never at 0, which would drop it from the results.
     *
     * @param volumesPerYear how many documents the archive holds per year
     * @param strength 0 leaves the ranking untouched
     * @return the function to send as the Solr boost, or null when the ranking shouldn't be touched
     */
    static String boostFunction(Map<String, Long> volumesPerYear, double strength) {
        Map<String, Double> weights = weights(volumesPerYear, strength);

        StringBuilder function = new StringBuilder("ms(" + YearVolumes.YEAR_FIELD + ")");
        boolean anyYearLifted = false;
        for (Map.Entry<String, Double> yearWeight : weights.entrySet()) {
            // The densest year, and every year already at 1, need no mapping at all
            if (yearWeight.getValue() <= NEUTRAL_WEIGHT) {
                continue;
            }
            int year = Integer.parseInt(yearWeight.getKey());
            function.insert(0, "map(")
                    .append(",").append(startOfYearMillis(year))
                    .append(",").append(startOfYearMillis(year + 1) - 1)
                    .append(",").append(String.format(Locale.ROOT, "%.4f", yearWeight.getValue()))
                    .append(")");
            anyYearLifted = true;
        }

        if (!anyYearLifted) {
            return null;
        }

        // Whatever no year matched is still an epoch value, and a missing date reads as 0 or below: both are neutral
        return "max(map(" + function + "," + EPOCH_LIKE_FLOOR + "," + EPOCH_LIKE_CEILING + ","
                + String.format(Locale.ROOT, "%.4f", NEUTRAL_WEIGHT) + "),"
                + String.format(Locale.ROOT, "%.4f", NEUTRAL_WEIGHT) + ")";
    }

    private static long startOfYearMillis(int year) {
        return LocalDate.of(year, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }
}
