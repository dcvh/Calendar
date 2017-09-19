package tcd.training.com.calendar.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import tcd.training.com.calendar.Entities.LunarDay;

/**
 * Created by ADMIN on 10/09/2017.
 */

public class TimeUtils {

    private static final String TAG = TimeUtils.class.getSimpleName();

    private static final int TYPE_DAY = 1;
    private static final int TYPE_MONTH = 2;
    private static final int TYPE_YEAR = 3;

    public static boolean isSameDay(long dayInMillis1, long dayInMillis2) {
        return compareTo(dayInMillis1, dayInMillis2, TYPE_DAY) == 0;
    }

    public static int compareDay(long dayInMillis1, long dayInMillis2) {
        return compareTo(dayInMillis1, dayInMillis2, TYPE_DAY);
    }

    public static int compareWeek(long weekInMillis1, long weekInMillis2) {

        Calendar week1 = Calendar.getInstance();
        week1.setTimeInMillis(weekInMillis1);
        Calendar week2 = Calendar.getInstance();
        week2.setTimeInMillis(weekInMillis2);

        int compareYear = week1.get(Calendar.YEAR) - week2.get(Calendar.YEAR);
        if (compareYear != 0) {
            return compareYear;
        } else {
            return week1.get(Calendar.WEEK_OF_YEAR) - week2.get(Calendar.WEEK_OF_YEAR);
        }
    }

    public static int compareMonth(long monthInMillis1, long monthInMillis2) {
        return compareTo(monthInMillis1, monthInMillis2, TYPE_MONTH);
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

    public static int getYear(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.YEAR);
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

    public static String getLunarString(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return getLunarString(calendar);
    }

    public static String getLunarString(Calendar calendar){

        LunarDay lunarDay = new LunarDay(calendar);

        String lunarString;
        if (lunarDay.isHoliday()) {
            lunarString = lunarDay.getLunar().getLunarHoliday();
            if (lunarString == null) {
                lunarString = lunarDay.getLunar().getSolarHolidy();
                if (lunarString == null) {
                    lunarString = lunarDay.getLunarDay();
                    if (lunarDay.isFirstDay()) {
                        lunarString += "/" + lunarDay.getLunar().getLunarMonth();
                    }
                }
            }
        } else {
            lunarString = lunarDay.getLunarDay();
            if (lunarDay.getLunar().getLunarDayNum() == 1) {
                lunarString += "/" + lunarDay.getLunar().getLunarMonth();
            }
        }
        return lunarString;
    }

    public static long getMillis(String date, String pattern) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        try {
            cal.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal.getTimeInMillis();
    }

    public static long getDurationValue(String duration) {

        int i = 0;
        for (; !Character.isDigit(duration.charAt(i)); i++);

        String value = duration.substring(i, duration.length() - 1);
        return Integer.valueOf(value) * 1000;
    }

    public static String getDurationString(long millis) {
        return "P" + millis / 1000 + "S";
    }
}
