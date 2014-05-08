package ApproxsimClient.object.primitive;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * A simple timestamp class where the internal representation is milliseconds from epoch
 * 
 * @version 1, $Date: 2006/05/11 13:24:41 $
 * @author Per Alexius
 */
public class Timestamp {

    /**
     * A time zone with offset 0 from UTC.
     */
    // Must be placed above xmlOutputFormat.
    private static TimeZone UTCTimeZone = new SimpleTimeZone(0, "UTC");

    /**
     * The default TimeZone to use for displaying and for user input.
     */
    private static TimeZone defaultTimeZone = TimeZone.getDefault();

    /**
     * The formatter used by this object.
     */
    private static DateFormat outputFormat = createOutputFormat();

    /**
     * The formatter used by this object.
     */
    private static DateFormat xmlOutputFormat = createXMLOutputFormat();

    /**
     * The date format used by this object.
     */
    private static DateFormat dateFormat = createDateFormat();

    /**
     * Number of milliseconds
     */
    private long mMilliSecs;

    /**
     * Obvious constructor
     * 
     * @param ms The number of milliseconds.
     */
    public Timestamp(long ms) {
        mMilliSecs = ms;
    }

    /**
     * Accessor for milliseconds
     * 
     * @return The number of milliseconds.
     */
    public long getMilliSecs() {
        return mMilliSecs;
    }

    /**
     * Creates the formatter used by this object.
     */
    private static DateFormat createOutputFormat() {
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS Z");
        format.setLenient(true);
        return format;
    }

    /**
     * Creates the formatter used for XML output.
     */
    private static DateFormat createXMLOutputFormat() {
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS");
        format.setTimeZone(UTCTimeZone);
        return format;
    }

    /**
     * Creates the date format for this object.
     */
    private static DateFormat createDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setLenient(true);
        return format;
    }

    /**
     * Returns a timestamp that is the specified duration added to this.
     */
    public Timestamp add(Duration duration) {
        return new Timestamp(mMilliSecs + duration.getMilliSecs());
    }

    /**
     * Returns true if this is the same time as other.
     */
    public boolean equals(Object o) {
        if (o instanceof Timestamp) {
            return ((Timestamp) o).getMilliSecs() == getMilliSecs();
        }
        return false;
    }

    /**
     * Returns this objects hashCode. (The same hashCode as for new Long(getMilliSecs()).hashCode())
     */
    public int hashCode() {
        return (int) (this.getMilliSecs() ^ (this.getMilliSecs() >>> 32));
    }

    /**
     * Creates a string representation of this Timestamp using the toString of java.util.Date.
     * 
     * @return A string representation of this Timestamp.
     */
    public String toString() {
        // DateFormat is not threadsafe, use a clone instead
        DateFormat myCopy = null;
        synchronized (Timestamp.outputFormat) {
            myCopy = (DateFormat) Timestamp.outputFormat.clone();
        };
        return myCopy.format(new java.util.Date(mMilliSecs));
    }

    /**
     * Creates a string representation of this Timestamp using the XML schema dateTime format. This method always normalizes to UTC.
     * 
     * @return A string representation of this Timestamp.
     */
    public String toDateTimeString() {
        // DateFormat is not threadsafe, use a clone instead
        DateFormat myCopy = null;
        synchronized (Timestamp.xmlOutputFormat) {
            myCopy = (DateFormat) Timestamp.xmlOutputFormat.clone();
        };
        return myCopy.format(new java.util.Date(mMilliSecs)) + "Z";
    }

    /**
     * Creates a Timestamp by parsing a string.
     * 
     * @param str the string to parse.
     */
    public static Timestamp parseTimestamp(String str) throws ParseException {
        // Just a number means number of milliseconds.
        if (str.matches("[0-9]+")) {
            return new Timestamp(Long.parseLong(str));
        }
        // XML Schema dateTime format.
        try {
            return new Timestamp(parseDateTime(str).getTime());
        } catch (ParseException e) {}

        try {
            // DateFormat is not threadsafe, use a clone instead
            DateFormat myCopy = null;
            synchronized (Timestamp.outputFormat) {
                myCopy = (DateFormat) Timestamp.outputFormat.clone();
            }
            return new Timestamp(myCopy.parse(str).getTime());
        } catch (ParseException e) {}

        // Only date format.
        DateFormat myCopy = null;
        synchronized (Timestamp.dateFormat) {
            myCopy = (DateFormat) Timestamp.dateFormat.clone();
        }
        return new Timestamp(myCopy.parse(str).getTime());
    }

    /**
     * Tries to parse the given string and make a Date object out of it. Vallid formats are at least:
     * <p>
     * XML Schema dateTime type
     * 
     * @param dateTime The string to parse.
     * @return The Date object created from the string.
     * @throws ParseException If parsing fails.
     */
    public static Date parseDateTime(String dateTime) throws ParseException {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss");
        dateTimeFormat.setTimeZone(UTCTimeZone);

        ParsePosition p = new ParsePosition(0);
        Date date = dateTimeFormat.parse(dateTime, p);
        if (date == null) {
            throw new ParseException("'" + dateTime
                    + "' is not a valid dateTime.", p.getErrorIndex());
        }

        String rest = dateTime.substring(p.getIndex());

        int millis = 0;
        int tzOffset = 0;
        // Check for milliseconds and time zone
        int tzStart = 0;
        int plus = rest.indexOf("+");
        int minus = rest.indexOf("-");
        int zed = rest.indexOf("Z"); // Zulu time i.e UTC.
        if (rest.length() > 0 && rest.charAt(0) == '.') {
            // Handle milliseconds
            double dmillis;
            try {
                if (zed > 0) {
                    dmillis = Double.parseDouble(rest.substring(0, zed));
                    tzStart = rest.length();
                } else if (plus > 0) {
                    dmillis = Double.parseDouble(rest.substring(0, plus));
                    tzStart = plus;
                } else if (minus > 0) {
                    dmillis = Double.parseDouble(rest.substring(0, minus));
                    tzStart = minus;
                } else {
                    dmillis = Double.parseDouble(rest);
                    tzStart = rest.length();
                }
            } catch (NumberFormatException e) {
                throw new ParseException(
                        "Unable to read milliseconds from the string '" + rest
                                + "'.", 0);
            }
            millis = (int) (dmillis * 1000);
        }
        if (zed == -1) {
            // Time zone.
            if (rest.length() > tzStart) {
                try {
                    int tzHour = Integer.parseInt(rest.substring(tzStart + 1,
                                                                 tzStart + 3));
                    int tzMin = Integer.parseInt(rest.substring(tzStart + 4));
                    tzOffset = (tzHour * 3600000 + tzMin * 60000)
                            * (plus != -1 ? 1 : -1);
                } catch (NumberFormatException e) {
                    throw new ParseException("'" + rest.substring(tzStart)
                            + "' is not a valid time zone format.", 0);
                }
            } else {
                tzOffset = defaultTimeZone.getOffset(new Date().getTime());
            }
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MILLISECOND, millis - tzOffset);
        return cal.getTime();
    }
}
