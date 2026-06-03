package engine.timeUtils;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Utility class for market-session state checks.
 *
 * <p>
 * Provides helpers for determining whether the market
 * is currently closed, reopening, or at the start of
 * a new trading day.
 * </p>
 *
 * <p>
 * Designed primarily for Forex-style weekly trading sessions.
 * </p>
 */
public class MarketSession {
    /**
     * Returns whether the market is currently considered closed
     * for weekend trading.
     *
     * <p>
     * Market close rules:
     * </p>
     *
     * <ul>
     * <li>Friday after 22:00</li>
     * <li>All Saturday</li>
     * <li>All Sunday</li>
     * <li>Monday before 02:00</li>
     * </ul>
     *
     * @return true if market is closed for weekend
     */
    public static boolean isWeekendOff(
            float gmtOffsetHours) {

        ZoneOffset offset = ZoneOffset.ofTotalSeconds(TimeUtils.gmtConverter(gmtOffsetHours));

        ZonedDateTime now = ZonedDateTime.now(
                ZoneId.ofOffset("UTC", offset));

        int weekday = now.getDayOfWeek().getValue();
        int hour = now.getHour();
        int minute = now.getMinute();

        double timeFloat = hour + (minute / 60.0);

        // Friday after 22:00
        if (weekday == 5 && timeFloat > 22.0) {
            return true;
        }

        // Saturday or Sunday
        if (weekday == 6 || weekday == 7) {
            return true;
        }

        // Monday before 02:00
        if (weekday == 1 && timeFloat < 2.0) {
            return true;
        }

        return false;
    }

    /**
     * Returns whether the current time is considered
     * the start of a trading day.
     *
     * <p>
     * Used for:
     * </p>
     *
     * <ul>
     * <li>Daily counter resets</li>
     * <li>Risk resets</li>
     * <li>Daily statistics refresh</li>
     * </ul>
     *
     * <p>
     * Conditions:
     * </p>
     *
     * <ul>
     * <li>Between 00:00 and 00:03</li>
     * <li>Monday at 02:00 market reopen</li>
     * </ul>
     *
     * @return true if current time is considered
     *         the beginning of a new trading day
     */
    public static boolean isDayStart(
            float gmtOffsetHours) {

        ZoneOffset offset = ZoneOffset.ofTotalSeconds(TimeUtils.gmtConverter(gmtOffsetHours));

        ZonedDateTime now = ZonedDateTime.now(
                ZoneId.ofOffset("UTC", offset));

        if (now.getHour() == 0 &&
                now.getMinute() < 4) {

            return true;

        } else if (now.getDayOfWeek() == DayOfWeek.MONDAY &&
                now.getHour() == 2) {

            return true;

        } else {

            return false;
        }
    }
}