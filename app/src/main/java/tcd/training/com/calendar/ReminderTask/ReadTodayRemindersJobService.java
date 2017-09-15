package tcd.training.com.calendar.ReminderTask;

import android.os.AsyncTask;
import android.os.Bundle;
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

import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Entry;
import tcd.training.com.calendar.Data.Event;
import tcd.training.com.calendar.Data.TimeUtils;

/**
 * Created by cpu10661-local on 9/7/17.
 */

public class ReadTodayRemindersJobService extends JobService {

    private final static String TAG = ReadTodayRemindersJobService.class.getSimpleName();

    private static final int SYNC_FLEXTIME_SECONDS = (int) TimeUnit.MINUTES.toSeconds(1);
    public static final String ARG_EVENT_ID = "eventId";
    public static final String ARG_EVENT_TITLE = "eventTitle";
    public static final String ARG_EVENT_START_TIME = "eventStartTime";
    public static final String ARG_EVENT_PRIORITY= "eventPriority";

    private AsyncTask mBackgroundTask;

    @Override
    public boolean onStartJob(final JobParameters job) {

        mBackgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                DataUtils.readEventPrioritiesFromFile(ReadTodayRemindersJobService.this);

                ArrayList<Event> events = getTodayAndTomorrowEvents();
                if (events != null) {
                    for (Event event : events) {
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

    private ArrayList<Event> getTodayAndTomorrowEvents() {

        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);

        ArrayList<Entry> entries = DataUtils.getEntriesBetween(this, start.getTimeInMillis(), start.getTimeInMillis() + TimeUnit.DAYS.toMillis(2));

        // assign to result
        ArrayList<Event> events = new ArrayList<>();
        for (Entry entry : entries) {
            events.addAll(entry.getEvents());
        }

        return events;
    }

    private int calculateReminderTime(Event event) {

        long time;

        // the event start time (00:00 if the event is all-day)
        if (event.isAllDay()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(event.getStartDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            time = cal.getTimeInMillis();
        } else {
            time = event.getStartDate();
        }

        time = TimeUnit.MILLISECONDS.toSeconds(time - Calendar.getInstance().getTimeInMillis());

        int beforeTime = (int) TimeUnit.MINUTES.toSeconds(DataUtils.getReminderMinutes(event.getId()));
        if (time > 0 && time - beforeTime < 0) {
            time = 0;
        } else {
            time -= beforeTime;
        }

        return (int) time;
    }

    private void scheduleEventReminder(Event event, int reminderTime) {

        // prepare data
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_EVENT_ID, event.getId());
        bundle.putString(ARG_EVENT_TITLE, event.getTitle());
        bundle.putLong(ARG_EVENT_START_TIME, event.getStartDate());
        bundle.putInt(ARG_EVENT_PRIORITY, event.getPriority());

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
