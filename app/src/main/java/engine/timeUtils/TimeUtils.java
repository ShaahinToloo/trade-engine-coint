package engine.timeUtils;

/**
 * General utility methods for time and timezone handling.
 */
public class TimeUtils {

    /**
     * Converts a GMT/UTC offset expressed in hours
     * into total offset seconds.
     *
     * <p>
     * Examples:
     * </p>
     *
     * <ul>
     *     <li>+3.0  -> 10800</li>
     *     <li>-5.0  -> -18000</li>
     *     <li>+5.5  -> 19800</li>
     * </ul>
     *
     * @param gmtOffsetHours GMT/UTC offset in hours
     *
     * @return timezone offset in seconds
     */
    public static int gmtConverter(float gmtOffsetHours) {
        return (int) (gmtOffsetHours * 60 * 60);
    }
}