package engine.trade;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple datetime holder with integer fields.
 *
 * <p>
 * Expected string format: {@code "yyyy-MM-dd HH:mm:ss"}
 * (same as DATETIME_INDEX entries).
 * </p>
 */
public class Timestamp {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    public int second;

    public Timestamp(int year, int month, int day, int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * Parses a datetime string and fills the int fields.
     *
     * @param datetimeStr e.g. {@code "2024-01-15 13:45:30"}
     */
    public Timestamp(String datetimeStr) {
        parse(datetimeStr);
    }

    /**
     * Fills the int fields from a datetime string.
     *
     * @param datetimeStr e.g. {@code "2024-01-15 13:45:30"}
     */
    public final void parse(String datetimeStr) {
        LocalDateTime dt = LocalDateTime.parse(datetimeStr, FORMATTER);
        this.year = dt.getYear();
        this.month = dt.getMonthValue();
        this.day = dt.getDayOfMonth();
        this.hour = dt.getHour();
        this.minute = dt.getMinute();
        this.second = dt.getSecond();
    }

    /**
     * Parses a datetime string into a new Timestamp.
     *
     * @param datetimeStr e.g. {@code "2024-01-15 13:45:30"}
     * @return parsed Timestamp, or null if input is null
     */
    public static Timestamp from(String datetimeStr) {
        if (datetimeStr == null) {
            return null;
        }
        return new Timestamp(datetimeStr);
    }

    public boolean isSameDay(Timestamp other) {
        if (other == null) {
            return false;
        }
        return this.year == other.year && this.month == other.month && this.day == other.day;
    }

    public boolean isSameMonth(Timestamp other) {
        if (other == null) {
            return false;
        }
        return this.year == other.year && this.month == other.month;
    }

    @Override
    public String toString() {
        return String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
    }
}
