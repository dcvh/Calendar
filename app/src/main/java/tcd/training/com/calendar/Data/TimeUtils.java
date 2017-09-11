package tcd.training.com.calendar.Data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ADMIN on 10/09/2017.
 */

public class TimeUtils {

    private static final int TYPE_DAY = 1;
    private static final int TYPE_MONTH = 2;
    private static final int TYPE_YEAR = 3;

    public static boolean isSameDay(long dayInMillis1, long dayInMillis2) {
        return compareTo(dayInMillis1, dayInMillis2, TYPE_DAY) == 0;
    }

    public static int compareDay(long dayInMillis1, long dayInMillis2) {
        return compareTo(dayInMillis1, dayInMillis2, TYPE_DAY);
    }

    public static int compareMonth(long monthInMillis1, long monthInMillis2) {
        return compareTo(monthInMillis1, monthInMillis2, TYPE_MONTH);
    }

    public static int compareYear(long yearInMillis1, long yearInMillis2) {
        return compareTo(yearInMillis1, yearInMillis2, TYPE_YEAR);
    }

    private static int compareTo(long millis1, long millis2, int type) {
        String pattern;
        switch (type) {
            case TYPE_DAY: pattern = "yyyyMMdd"; break;
            case TYPE_MONTH: pattern = "yyyyMM"; break;
            case TYPE_YEAR: pattern = "yyyy"; break;
            default:
                throw new UnsupportedOperationException("TimeUtils - compare: Unknown comparison type");
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.getDefault());
        return formatter.format(new Date(millis1)).compareTo(formatter.format(new Date(millis2)));
    }

    public static String getFormattedDate(long millis, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return formatter.format(calendar.getTime());
    }

    public static String getStandardTimeFormat() {
        return "hh:mm a";
    }
}
