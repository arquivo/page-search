package pt.arquivo.services.solr;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class YearBalanceTest {

    /** An archive holding ten times more documents on 2008 than on 1997, which is the case this exists for. */
    private static Map<String, Long> archive() {
        Map<String, Long> volumes = new LinkedHashMap<>();
        volumes.put("1997", 100000L);
        volumes.put("2008", 1000000L);
        return volumes;
    }

    @Test
    public void thinYearsAreLiftedAndDenseYearsAreLeftAlone() {
        Map<String, Double> weights = YearBalance.weights(archive(), 1.0);

        // Normalized all the way, the thin year is lifted by exactly the ratio between the two
        assertThat(weights.get("1997")).isCloseTo(10.0, within(0.0001));
        // The densest year is the one every other is measured against, so it stays where it is
        assertThat(weights.get("2008")).isCloseTo(1.0, within(0.0001));
    }

    @Test
    public void strengthDampensTheLift() {
        // Half strength is the square root of the ratio, a lift instead of a full normalization
        assertThat(YearBalance.weights(archive(), 0.5).get("1997")).isCloseTo(3.1623, within(0.0001));
        assertThat(YearBalance.weights(archive(), 0.25).get("1997")).isCloseTo(1.7783, within(0.0001));
    }

    @Test
    public void noStrengthLeavesTheRankingUntouched() {
        assertThat(YearBalance.weights(archive(), 0)).isEmpty();
        assertThat(YearBalance.boostFunction(archive(), 0)).isNull();
    }

    @Test
    public void anArchiveThatCouldntBeMeasuredLeavesTheRankingUntouched() {
        Map<String, Long> empty = new LinkedHashMap<>();
        assertThat(YearBalance.boostFunction(empty, 1.0)).isNull();

        // Volumes that are all zero say nothing about which years are thin
        Map<String, Long> zeroed = new LinkedHashMap<>();
        zeroed.put("1997", 0L);
        assertThat(YearBalance.boostFunction(zeroed, 1.0)).isNull();
    }

    @Test
    public void yearsTheArchiveHasNothingForStayNeutral() {
        Map<String, Long> volumes = archive();
        volumes.put("2030", 0L);

        // Dividing by an empty year would lift it out of every proportion, and it has nothing to rank anyway
        assertThat(YearBalance.weights(volumes, 1.0).get("2030")).isCloseTo(1.0, within(0.0001));
        assertThat(YearBalance.boostFunction(volumes, 1.0)).doesNotContain("1893456000000");
    }

    @Test
    public void boostFunctionMapsEachYearOntoItsWeight() {
        String boostFunction = YearBalance.boostFunction(archive(), 1.0);

        // 1997 is the only year worth mapping, from the first millisecond of the year to its last
        assertThat(boostFunction).isEqualTo(
                "max(map(map(ms(dateOldest),852076800000,883612799999,10.0000),1000000000,100000000000000,1.0000),1.0000)");
    }

    @Test
    public void boostFunctionIsNeutralForDocumentsOutsideTheArchiveYears() {
        String boostFunction = YearBalance.boostFunction(archive(), 1.0);

        // A document dated outside the mapped years keeps looking like an epoch value, so it is mapped to 1.0, and a
        // document without a date reads as 0 or below, which the max() takes back to 1.0. Neither may come out at 0:
        // a multiplicative boost of 0 would drop the document from the results.
        assertThat(boostFunction).startsWith("max(map(").endsWith(",1000000000,100000000000000,1.0000),1.0000)");
    }

    @Test
    public void weightsAreWrittenWithADecimalPointWhateverTheLocale() {
        Locale defaultLocale = Locale.getDefault();
        try {
            // Portugal writes 3,1623, which Solr would read as two arguments
            Locale.setDefault(new Locale("pt", "PT"));
            assertThat(YearBalance.boostFunction(archive(), 0.5)).contains("3.1623").doesNotContain(",3,1623");
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}
