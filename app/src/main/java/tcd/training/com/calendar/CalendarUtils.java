package tcd.training.com.calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarUtils {

    private static final String TAG = CalendarUtils.class.getSimpleName();

    public static ArrayList<CalendarEntry> readCalendarEvent(Context context) {

        ArrayList<CalendarEntry> eventEntries = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return eventEntries;
        }

        // prepare Uri
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String[] projections = new String[]{
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
        };

        // querying
        Cursor cursor = contentResolver.query(uri, projections, null, null, null);
        if (cursor == null) {
            Log.e(TAG, "readCalendarEvent: There was a problem handling the cursor");
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(context, R.string.no_calendar_events_error, Toast.LENGTH_SHORT).show();
        } else {
            do {
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String description = cursor.getString(2);
                long startDate = cursor.getLong(3);
                long endDate = cursor.getLong(4);
                
                CalendarEvent event = new CalendarEvent(id, title, description, startDate, endDate);

                String dateOfEvent = getDate(startDate, "yyyy/MM/dd");
                for (CalendarEntry entry : eventEntries) {
                    if (entry.getDate().equals(dateOfEvent)) {
                        entry.addEvent(event);
                        continue;
                    }
                }
                CalendarEntry newEventDate = new CalendarEntry(dateOfEvent, new ArrayList<>(Arrays.asList(event)));
                eventEntries.add(newEventDate);
            } while (cursor.moveToNext());
        }

        // clean up
        assert cursor != null;
        cursor.close();

        return eventEntries;
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
