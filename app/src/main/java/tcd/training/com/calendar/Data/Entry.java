package tcd.training.com.calendar.Data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class Entry implements Comparable<Entry> {

    private long mMillis;
    private String mDescription;
    private ArrayList<Event> mEvents;

    public Entry(long millis, ArrayList<Event> events) {
        this.mMillis = millis;
        this.mEvents = events;
    }

    public Entry(long millis, String description, ArrayList<Event> events) {
        this.mMillis = millis;
        this.mDescription = description;
        this.mEvents = events;
    }

    public long getTime() {
        return mMillis;
    }

    public String getDescription() {
        return mDescription;
    }

    public ArrayList<Event> getEvents() {
        return mEvents;
    }

    public void addEvent(Event event) {
        mEvents.add(event);
    }

    @Override
    public int compareTo(@NonNull Entry entry) {
        long millis = entry.getTime();
        if (mMillis == millis) {
            return 0;
        } else if (mMillis > millis) {
            return 1;
        } else {
            return -1;
        }
    }
}
