package tcd.training.com.calendar.Calendar;

/**
 * Created by cpu10661-local on 9/6/17.
 */

public class Reminder {

    private int mId;
    private int mEventId;
    private int mMinutes;
    private int mMethod;

    public Reminder(int id, int eventId, int minutes, int method) {
        this.mId = id;
        this.mEventId = eventId;
        this.mMinutes = minutes;
        this.mMethod = method;
    }

    public int getId() {
        return mId;
    }

    public int getEventId() {
        return mEventId;
    }

    public int getMinutes() {
        return mMinutes;
    }

    public int getMethod() {
        return mMethod;
    }
}
