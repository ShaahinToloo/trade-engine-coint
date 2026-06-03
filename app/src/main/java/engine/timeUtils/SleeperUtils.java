package engine.timeUtils;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import engine.constants.PublicConstants;

/**
 * Utility class for time-based execution pauses used in live trading.
 *
 * <p>
 * Provides synchronized sleeping methods for:
 * </p>
 *
 * <ul>
 * <li>Waiting for the next candle/open interval</li>
 * <li>Pausing execution during weekends</li>
 * <li>Suspending trading after daily loss limits are reached</li>
 * </ul>
 *
 * <p>
 * All time calculations are performed using the configured GMT offset.
 * </p>
 */
public class SleeperUtils {
    /**
     * Suspends execution until the next interval boundary.
     *
     * <p>
     * Supports fractional-minute intervals such as:
     * </p>
     *
     * <ul>
     * <li>5.0 -> every 5 minutes</li>
     * <li>1.0 -> every minute</li>
     * <li>0.5 -> every 30 seconds</li>
     * <li>0.25 -> every 15 seconds</li>
     * </ul>
     *
     * <p>
     * A configurable safety buffer is added to reduce the risk
     * of reading unfinished market candles.
     * </p>
     *
     * @param intervalMinutes interval size in minutes
     * @param bufferSeconds   safety delay added after boundary
     */
    public static long sleepUntilNextInterval(
            double intervalMinutes,
            int bufferSeconds) {

        if (intervalMinutes <= 0) {
            throw new IllegalArgumentException(
                    "intervalMinutes must be > 0");
        }

        ZoneOffset offset = PublicConstants.ZONE_OFFSET;

        ZonedDateTime now = ZonedDateTime.now(
                ZoneId.ofOffset("UTC", offset));

        long intervalMillis = (long) (intervalMinutes * 60_000);

        long nowMillis = now.toInstant().toEpochMilli();

        long nextBoundaryMillis = ((nowMillis / intervalMillis) + 1)
                * intervalMillis;

        long sleepMillis = (nextBoundaryMillis - nowMillis)
                + (bufferSeconds * 1000L);

        if (sleepMillis > 0) {

            double sleepSeconds = sleepMillis / 1000.0;

            System.out.printf(
                    "Sleeping %.2f seconds until next interval...\n",
                    sleepSeconds);

            PublicConstants.sleep(sleepMillis);
        }
        return nextBoundaryMillis;
    }

    public static void sleepUntilGivenBoundary(
            long nextBoundaryMillis,
            int bufferSeconds) {

        if (nextBoundaryMillis <= 0) {
            throw new IllegalArgumentException(
                    "nextBoundaryMillis must be > 0");
        }

        ZoneOffset offset = PublicConstants.ZONE_OFFSET;

        ZonedDateTime now = ZonedDateTime.now(
                ZoneId.ofOffset("UTC", offset));

        long nowMillis = now.toInstant().toEpochMilli();

        long sleepMillis = (nextBoundaryMillis - nowMillis)
                + (bufferSeconds * 1000L);

        if (sleepMillis > 0) {

            double sleepSeconds = sleepMillis / 1000.0;

            System.out.printf(
                    "Sleeping %.2f seconds until next Boundary...\n",
                    sleepSeconds);

            PublicConstants.sleep(sleepMillis);
        }
    }

    /**
     * Suspends execution during weekends until Monday 02:00.
     *
     * <p>
     * Intended for markets that close during weekends (e.g. Forex).
     * </p>
     *
     * <p>
     * Uses the configured timezone offset to determine the correct
     * reopening time.
     * </p>
     */
    public static void sleepWeekend() {
        ZoneOffset offset = PublicConstants.ZONE_OFFSET;
        ZonedDateTime now = ZonedDateTime.now(ZoneId.ofOffset("UTC", offset));

        // Calculate days until next Monday
        int dayOfWeek = now.getDayOfWeek().getValue(); // Monday = 1 ... Sunday = 7
        int daysUntilMonday = (8 - dayOfWeek) % 7;

        // target next Monday
        if (!(daysUntilMonday == 0 && now.getHour() < 2)) {
            daysUntilMonday = (7 - dayOfWeek + 1) % 7;

            if (daysUntilMonday == 0) {
                daysUntilMonday = 7;
            }
        }

        ZonedDateTime target = now
                .plusDays(daysUntilMonday)
                .withHour(2)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        long sleepSeconds = ChronoUnit.SECONDS.between(now, target);
        double sleepHours = sleepSeconds / 3600.0;

        System.out.println(String.format(
                "Weekend detected. Sleeping until Monday 02:00 (%,.1f hours)",
                sleepHours));

        PublicConstants.sleep(sleepSeconds * 1000L);
    }

    /**
     * Suspends execution until the start of the next day.
     *
     * <p>
     * Intended for risk-control systems that halt trading after
     * reaching a daily drawdown or loss threshold.
     * </p>
     *
     * <p>
     * The method sleeps until midnight in the configured timezone.
     * </p>
     */
    public static void sleepUntilNextDay() {
        ZoneOffset offset = PublicConstants.ZONE_OFFSET;

        ZonedDateTime now = ZonedDateTime.now(
                ZoneId.ofOffset("UTC", offset));

        ZonedDateTime nextDay = now
                .truncatedTo(ChronoUnit.DAYS)
                .plusDays(1);

        long secondsUntilNextDay = ChronoUnit.SECONDS.between(now, nextDay);

        double hoursUntilNextDay = secondsUntilNextDay / 3600.0;

        System.out.println(String.format(
                "Daily loss threshold reached. Sleeping until next day (%.2f hours)",
                hoursUntilNextDay));

        PublicConstants.sleep(secondsUntilNextDay * 1000L);
    }
}