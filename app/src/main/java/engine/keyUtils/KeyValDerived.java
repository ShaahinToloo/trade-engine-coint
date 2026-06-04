package engine.keyUtils;

public class KeyValDerived {
    public static final String HOUR_SIN;
    public static final String HOUR_COS;
    public static final String DOW_SIN;
    public static final String DOW_COS;
    public static final String MINUTE_OF_DAY;
    public static final String SESSION_TOKYO;
    public static final String SESSION_LONDON;
    public static final String SESSION_NY;
    public static final String SESSION_LON_NY_OVERLAP;
    public static final String SESSION_TOK_LON_OVERLAP;
    public static final String SESSION_ID;
    public static final String MINS_SINCE_TOKYO_OPEN;
    public static final String MINS_SINCE_LONDON_OPEN;
    public static final String MINS_SINCE_NY_OPEN;
    public static final String MINS_TO_TOKYO_CLOSE;
    public static final String MINS_TO_LONDON_CLOSE;
    public static final String MINS_TO_NY_CLOSE;

    public static final String RAD_SLOPE_YPORT;

    static {
        // Time Features
        HOUR_SIN = KeyGen.build().type("time").source("hour").target("sin").build();
        HOUR_COS = KeyGen.build().type("time").source("hour").target("cos").build();
        DOW_SIN = KeyGen.build().type("time").source("dow").target("sin").build();
        DOW_COS = KeyGen.build().type("time").source("dow").target("cos").build();
        MINUTE_OF_DAY = KeyGen.build().type("time").source("minute_of_day").build();
        SESSION_TOKYO = KeyGen.build().type("session").source("tokyo").build();
        SESSION_LONDON = KeyGen.build().type("session").source("london").build();
        SESSION_NY = KeyGen.build().type("session").source("ny").build();
        SESSION_LON_NY_OVERLAP = KeyGen.build().type("session").source("lon_ny_overlap").build();
        SESSION_TOK_LON_OVERLAP = KeyGen.build().type("session").source("tok_lon_overlap").build();
        SESSION_ID = KeyGen.build().type("session").source("id").build();
        MINS_SINCE_TOKYO_OPEN = KeyGen.build().type("session_time").source("tokyo").target("since_open").build();
        MINS_SINCE_LONDON_OPEN = KeyGen.build().type("session_time").source("london").target("since_open").build();
        MINS_SINCE_NY_OPEN = KeyGen.build().type("session_time").source("ny").target("since_open").build();
        MINS_TO_TOKYO_CLOSE = KeyGen.build().type("session_time").source("tokyo").target("to_close").build();
        MINS_TO_LONDON_CLOSE = KeyGen.build().type("session_time").source("london").target("to_close").build();
        MINS_TO_NY_CLOSE = KeyGen.build().type("session_time").source("ny").target("to_close").build();

        RAD_SLOPE_YPORT = KeyGen.build().type("radSlope").source("yPort").build();
    }
}
