// $Id: Duration.java,v 1.2 2006/03/23 17:16:51 alexius Exp $
/*
 * @(#)Duration.java
 */

package ApproxsimClient.object.primitive;

import java.text.ParseException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.Hashtable;

/**
 * A simple duration class where the internal representation is milliseconds
 * 
 * @version 1, $Date: 2006/03/23 17:16:51 $
 * @author Daniel Ahlin
 */
public class Duration {
    /**
     * MilliSecs per second
     */
    static long TICS_PER_SECOND = 1000;

    /**
     * MilliSecs per minute
     */
    static long TICS_PER_MINUTE = 60 * TICS_PER_SECOND;

    /**
     * MilliSecs per hour
     */
    static long TICS_PER_HOUR = 60 * TICS_PER_MINUTE;

    /**
     * MilliSecs per day
     */
    static long TICS_PER_DAY = 24 * TICS_PER_HOUR;

    /**
     * MilliSecs per week
     */
    static long TICS_PER_WEEK = 7 * TICS_PER_DAY;

    /**
     * MilliSecs per year
     */
    static long TICS_PER_YEAR = 365 * TICS_PER_DAY;

    /**
     * Unit to multiplier lookup table.
     */
    static Hashtable<String, Long> unitLookupTable = createUnitLookupTable();

    /**
     * Number of milliseconds
     */
    private long milliSecs;

    /**
     * Creates a new duration.
     * 
     * @param ms The number of milliseconds.
     */
    public Duration(long ms) {
        milliSecs = ms;
    }

    /**
     * Accessor for milliseconds
     * 
     * @return The number of milliseconds.
     */
    public long getMilliSecs() {
        return milliSecs;
    }

    /**
     * Creates a string representation of this Duration
     * 
     * @return A string representation of this Duration.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        long remainder = getMilliSecs();
        if (remainder < 0) {
            remainder *= -1;
            buf.append("-");
        }

        long years = remainder / TICS_PER_YEAR;
        if (years > 0) {
            buf.append(years + "y ");
            remainder %= TICS_PER_YEAR;
        }

        long weeks = remainder / TICS_PER_WEEK;
        if (weeks > 0) {
            buf.append(weeks + "w ");
            remainder %= TICS_PER_WEEK;
        }

        long days = remainder / TICS_PER_DAY;
        if (days > 0) {
            buf.append(days + "d ");
            remainder %= TICS_PER_DAY;
        }

        long hours = remainder / TICS_PER_HOUR;
        if (hours > 0) {
            buf.append(hours + "h ");
            remainder %= TICS_PER_HOUR;
        }

        long minutes = remainder / TICS_PER_MINUTE;
        if (minutes > 0) {
            buf.append(minutes + "m ");
            remainder %= TICS_PER_MINUTE;
        }

        long seconds = remainder / TICS_PER_SECOND;
        if (seconds > 0) {
            buf.append(seconds + "s ");
            remainder %= TICS_PER_SECOND;
        }

        if (remainder > 0) {
            buf.append(remainder + "ms ");
        }

        return buf.toString().trim();
    }

    /**
     * Creates a Duration by parsing a string.
     * 
     * @param str the string to parse.
     */
    public static Duration parseDuration(String str) throws ParseException {
        long res = 0;
        boolean negative = false;

        // Check for negative pattern
        if (str.matches("\\A-.*")) {
            negative = true;
            str = str.substring(1);
        }

        // If no unit and only numericals we assume seconds.
        if (str.matches("\\A[0-9]+\\z")) {
            return new Duration(Long.parseLong(str));
        }

        Pattern splitPattern = Pattern.compile("\\s+");

        String[] fields = splitPattern.split(str);

        for (int i = 0; i < fields.length; i++) {
            String value;
            String unit;
            if (fields[i].matches("\\A[0-9]+\\z") && i < (fields.length - 1)) {
                // Check for unit in next field
                value = fields[i];
                unit = fields[i + 1];
                i++;
            } else if (fields[i].matches("\\A[0-9]+[^0-9]+\\z")) {
                // Check for unit as tail of field.
                Matcher matcher = Pattern.compile("[^0-9].*\\z")
                        .matcher(fields[i]);
                matcher.find();
                int limit = matcher.start();
                value = fields[i].substring(0, limit);
                unit = fields[i].substring(limit);
            } else {
                // Else there is an errror;
                throw new ParseException("Unable to parse \"" + fields[i]
                        + "\"", -1);
            }

            // Get multiplier;
            Long multiplier = (Long) unitLookupTable.get(unit.toLowerCase());
            if (multiplier == null) {
                throw new ParseException("Unable to parse \"" + unit + "\"", -1);
            } else {
                res += (Long.parseLong(value) * multiplier.longValue());

            }
        }

        if (negative) {
            res *= -1;
        }
        return new Duration(res);
    }

    /**
     * Creates a lookup table matching unit strings to tics multipliers.
     */
    private static Hashtable<String, Long> createUnitLookupTable() {
        Hashtable<String, Long> res = new Hashtable<String, Long>();

        res.put("ms", new Long(1));
        res.put("millisecond", new Long(1));
        res.put("milliseconds", new Long(1));

        res.put("s", new Long(TICS_PER_SECOND));
        res.put("second", new Long(TICS_PER_SECOND));
        res.put("seconds", new Long(TICS_PER_SECOND));

        res.put("m", new Long(TICS_PER_MINUTE));
        res.put("minute", new Long(TICS_PER_MINUTE));
        res.put("minutes", new Long(TICS_PER_MINUTE));

        res.put("h", new Long(TICS_PER_HOUR));
        res.put("hour", new Long(TICS_PER_HOUR));
        res.put("hours", new Long(TICS_PER_HOUR));

        res.put("d", new Long(TICS_PER_DAY));
        res.put("day", new Long(TICS_PER_DAY));
        res.put("days", new Long(TICS_PER_DAY));

        res.put("w", new Long(TICS_PER_WEEK));
        res.put("week", new Long(TICS_PER_WEEK));
        res.put("weeks", new Long(TICS_PER_WEEK));

        res.put("y", new Long(TICS_PER_YEAR));
        res.put("year", new Long(TICS_PER_YEAR));
        res.put("years", new Long(TICS_PER_YEAR));

        return res;
    }

    /**
     * Returns true if this is the same time as other.
     */
    public boolean equals(Object o) {
        if (o instanceof Duration) {
            return ((Duration) o).getMilliSecs() == getMilliSecs();
        }
        return false;
    }

    /**
     * Returns this objects hashCode. (The same hashCode as for new Long(getMilliSecs()).hashCode())
     */
    public int hashCode() {
        return (int) (this.getMilliSecs() ^ (this.getMilliSecs() >>> 32));
    }
}
