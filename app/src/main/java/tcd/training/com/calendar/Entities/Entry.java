package tcd.training.com.calendar.Entities;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class Entry implements Comparable<Entry> {

    private long mMillis;
    private String mDescription;
    private ArrayList<Event> mEvents;

    public Entry(long millis, ArrayList<Event> events) {
        mMillis = millis;
        mEvents = events;
    }

    public Entry(long millis, String description, ArrayList<Event> events) {
        mMillis = millis;
        mDescription = description;
        mEvents = events;
    }

    public Entry(Entry entry) {
        mMillis = entry.getTime();
        mDescription = entry.getDescription();

        mEvents = new ArrayList<>();
        for (Event event : entry.getEvents()) {
            mEvents.add(event);
        }
    }

    public long getTime() {
        return mMillis;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
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
