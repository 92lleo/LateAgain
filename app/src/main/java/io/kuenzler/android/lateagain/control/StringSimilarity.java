package io.kuenzler.android.lateagain.control;

/**
 * @author Leonhard Künzler
 * @version 1.0
 * @date 15.11.03 01:00
 */
public class StringSimilarity {

    /**
     * Returns levenstein distance between two strings as double value
     *
     * @param s1 String to compare to s2
     * @param s2 String to comapre to s1
     * @return levenstein distance s1&s2 as double percentage, -1 if s1 or s2 is
     * empty
     */
    public static double getSimilarityPercent(String s1, String s2) {
        StringSimilarity ss = new StringSimilarity();
        if (s1.trim().isEmpty() || s2.trim().isEmpty()) {
            return -1;
        }
        return ss.getStringAccordance(s1, s2);
    }

    /**
     * Returns levenstein Distance of 2 strings as int.
     * http://rosettacode.org/wiki/Levenshtein_distance#Java
     *
     * @param a First String
     * @param b Second String
     * @return Levenstein Distance
     */
    private int calculateLevensteinDistance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    /**
     * Returns Accordance of 2 Strings as doble (percent).
     *
     * @param a First String
     * @param b Second String
     * @return accordance of 2 strings from levenstein distance (percent)
     */
    public double getStringAccordance(final String a, final String b) {
        int lfd = calculateLevensteinDistance(a, b);
        double ratio = ((double) lfd) / (Math.max(a.length(), b.length()));
        ratio = 100 - ratio * 100; // prozentberechnung
        return ratio;
    }
}
