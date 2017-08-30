package tcd.training.com.calendar;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarEntry implements Comparable {

    private String mDate;
    private ArrayList<CalendarEvent> mEvents;

    public CalendarEntry(String startDate, ArrayList<CalendarEvent> events) {
        this.mDate = startDate;
        this.mEvents = events;
    }

    public String getDate() {
        return mDate;
    }

    public ArrayList<CalendarEvent> getEvents() {
        return mEvents;
    }

    public void addEvent(CalendarEvent event) {
        mEvents.add(event);
    }

    @Override
    public int compareTo(@NonNull Object object) {
        String date = ((CalendarEntry)object).getDate();
        return mDate.compareTo(date);
    }
}
