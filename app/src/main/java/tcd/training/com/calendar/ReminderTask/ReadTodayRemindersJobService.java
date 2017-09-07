package tcd.training.com.calendar.ReminderTask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.Calendar.CalendarEntry;
import tcd.training.com.calendar.Calendar.CalendarEvent;
import tcd.training.com.calendar.Calendar.CalendarUtils;
import tcd.training.com.calendar.Calendar.Reminder;

/**
 * Created by cpu10661-local on 9/7/17.
 */

public class ReadTodayRemindersJobService extends JobService {

    private final static String TAG = ReadTodayRemindersJobService.class.getSimpleName();

    private static final int SYNC_FLEXTIME_SECONDS = (int) TimeUnit.MINUTES.toSeconds(1);
    public static final String ARG_EVENT_ID = "eventId";
    public static final String ARG_EVENT_TITLE = "eventTitle";
    public static final String ARG_EVENT_START_TIME = "eventStartTime";

    private AsyncTask mBackgroundTask;

    @Override
    public boolean onStartJob(final JobParameters job) {

        mBackgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                ArrayList<CalendarEvent> events = getTodayAndTomorrowEvents();
                if (events != null) {
                    for (CalendarEvent event : events) {
                        int reminderTime = calculateReminderTime(event);
                        Log.e(TAG, "doInBackground: " + reminderTime);
                        if (reminderTime < 0) {
                            continue;
                        }
                        scheduleEventReminder(event, reminderTime);
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(job, false);
            }
        };

        mBackgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mBackgroundTask != null) {
            mBackgroundTask.cancel(true);
        }
        return true;
    }

    private ArrayList<CalendarEvent> getTodayAndTomorrowEvents() {

        // today events
        Calendar today = Calendar.getInstance();
        String todayString = CalendarUtils.getDate(today.getTimeInMillis(), CalendarUtils.getStandardDateFormat());
        CalendarEntry todayEntry = CalendarUtils.findEntryWithDate(todayString);

        // tomorrow events
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        String tomorrowString = CalendarUtils.getDate(tomorrow.getTimeInMillis(), CalendarUtils.getStandardDateFormat());
        CalendarEntry tomorrowEntry = CalendarUtils.findEntryWithDate(tomorrowString);

        // assign to result
        ArrayList<CalendarEvent> events = todayEntry != null ? todayEntry.getEvents() : null;
        if (tomorrowEntry != null) {
            if (events != null) {
                events.addAll(tomorrowEntry.getEvents());
            } else {
                events = tomorrowEntry.getEvents();
            }
        }

        return events;
    }

    private int calculateReminderTime(CalendarEvent event) {

        long time;

        // the event start time (00:00 if the event is all-day)
        if (event.isAllDay()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(event.getStartDate());
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            time = cal.getTimeInMillis();
        } else {
            time = event.getStartDate();
        }

        time = TimeUnit.MILLISECONDS.toSeconds(time - Calendar.getInstance().getTimeInMillis());

        int beforeTime = (int) TimeUnit.MINUTES.toSeconds(CalendarUtils.getReminderMinutes(event.getId()));
        time -= beforeTime;

        return (int) time;
    }

    private void scheduleEventReminder(CalendarEvent event, int reminderTime) {

        // prepare data
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_EVENT_ID, event.getId());
        bundle.putString(ARG_EVENT_TITLE, event.getTitle());
        bundle.putLong(ARG_EVENT_START_TIME, event.getStartDate());

        // prepare dispatcher
        Driver driver = new GooglePlayDriver(ReadTodayRemindersJobService.this);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        // start scheduling
        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(ReminderJobService.class)
                .setTag(String.valueOf(event.getId()))
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(reminderTime, reminderTime + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .setExtras(bundle)
                .build();
        dispatcher.schedule(constraintReminderJob);
    }
}
