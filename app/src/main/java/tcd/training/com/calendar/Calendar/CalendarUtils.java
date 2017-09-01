package tcd.training.com.calendar.Calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarUtils {

    private static final String TAG = CalendarUtils.class.getSimpleName();

    private static ArrayList<CalendarEntry> mEntries;

    public static ArrayList<CalendarEntry> getAllEntries() {
        return mEntries;
    }

    public static ArrayList<CalendarEntry> readCalendarEvent(Context context) {

        ArrayList<CalendarEntry> eventEntries = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return eventEntries;
        }

        // prepare Uri
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        LinkedHashMap<String, Integer> projections = new LinkedHashMap<>();
        int i = 0;
        projections.put(CalendarContract.Events._ID, i++);
        projections.put(CalendarContract.Events.TITLE, i++);
        projections.put(CalendarContract.Events.ORGANIZER, i++);
        projections.put(CalendarContract.Events.EVENT_LOCATION, i++);
        projections.put(CalendarContract.Events.DESCRIPTION, i++);
        projections.put(CalendarContract.Events.DTSTART, i++);
        projections.put(CalendarContract.Events.DTEND, i++);
        projections.put(CalendarContract.Events.ALL_DAY, i++);

        // querying
        Cursor cursor = contentResolver.query(uri, projections.keySet().toArray(new String[0]), null, null, null);
        if (cursor == null) {
            Log.e(TAG, "readCalendarEvent: There was a problem handling the cursor");
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(context, R.string.no_calendar_events_error, Toast.LENGTH_SHORT).show();
        } else {
            do {
                long id = cursor.getLong(projections.get(CalendarContract.Events._ID));
                String title = cursor.getString(projections.get(CalendarContract.Events.TITLE));
                String organizer = cursor.getString(projections.get(CalendarContract.Events.ORGANIZER));
                String location = cursor.getString(projections.get(CalendarContract.Events.EVENT_LOCATION));
                String description = cursor.getString(projections.get(CalendarContract.Events.DESCRIPTION));
                long startDate = cursor.getLong(projections.get(CalendarContract.Events.DTSTART));
                long endDate = cursor.getLong(projections.get(CalendarContract.Events.DTEND));
                boolean allDay = cursor.getInt(projections.get(CalendarContract.Events.ALL_DAY)) == 1;

                if (title.length() == 0) {
                    title = context.getString(R.string.no_title);
                }

                CalendarEvent event = new CalendarEvent(id, title, organizer, location, description, startDate, endDate, allDay);

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

        mEntries = eventEntries;
        return eventEntries;
    }

    /**
     *
     * @param date must be in "yyyy/MM/dd" format
     * @return index of the nearest (smaller) event's date in the entries, -1 if all events occur after the specified date
     */
    public static int findNearestDateBefore(String date, ArrayList<CalendarEntry> entries) {
        int index = 0;
        for (CalendarEntry entry : entries) {
            if (date.compareTo(entry.getDate()) > 0) {
                index++;
                continue;
            }
            index--;
            break;
        }
        return index;
    }

    /**
     *
     * @param date must be in "yyyy/MM/dd" format
     * @return entry with the specified date, null if there is no entry matches the date
     */
    public static CalendarEntry findEntryWithDate(String date) {
        // TODO: 9/1/17 this is temporary, must be fixed in the future for better performance (consider switching to binary search)
        assert mEntries != null;
        assert date.length() == 10;
        for (int i = 0; i< mEntries.size(); i++) {
            if (mEntries.get(i).getDate().equals(date)) {
                return mEntries.get(i);
            }
        }
        return null;
    }

    /**
     *
     * @return today in "yyyy/MM/dd" format
     */
    public static String getToday() {
        Calendar calendar = Calendar.getInstance();
        return getDate(calendar.getTimeInMillis(), "yyyy/MM/dd");
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
