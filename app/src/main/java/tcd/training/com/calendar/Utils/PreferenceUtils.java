package tcd.training.com.calendar.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import tcd.training.com.calendar.R;

/**
 * Created by cpu10661 on 9/21/17.
 */

public class PreferenceUtils {

    private static final String TAG = PreferenceUtils.class.getSimpleName();

    public static int getFirstDayOfWeek(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String firstDay = sharedPreferences.getString(context.getString(R.string.pref_key_start_of_the_week), "monday");

        switch (firstDay) {
            case "saturday":
                return Calendar.SATURDAY;
            case "sunday":
                return Calendar.SUNDAY;
            case "monday":
                return Calendar.MONDAY;
            default:
                Log.e(TAG, "getFirstDayOfWeek: " + firstDay);
                throw new UnsupportedOperationException("Unknown first day");
        }
    }

    public static String[] getDayOfWeekOrder(int firstDayOfWeek, Context context) {
        switch (firstDayOfWeek) {
            case Calendar.SATURDAY:
                return context.getResources().getStringArray(R.array.first_day_saturday);
            case Calendar.SUNDAY:
                return context.getResources().getStringArray(R.array.first_day_sunday);
            case Calendar.MONDAY:
                return context.getResources().getStringArray(R.array.first_day_monday);
            default:
                throw new UnsupportedOperationException("Unknown first day of week");
        }
    }

    public static String getAlternateCalendar(Context context) {
        String alternate = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.pref_key_alternate_calendar), context.getString(R.string.pref_alternate_calendar_default));

        return alternate.equals(context.getString(R.string.pref_alternate_calendar_default)) ? null : alternate;
    }

    public static String getAlternateDate(long millis, String alternate) {
        switch (alternate) {
            case "chinese": return TimeUtils.getLunarString(millis);
            default:
                return null;
        }
    }

    public static boolean isShowNumberOfWeekChecked(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_show_week_number), false);
    }

    public static String getLanguage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.pref_key_language), context.getString(R.string.pref_language_default));
    }
}
