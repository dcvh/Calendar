package tcd.training.com.calendar.Data;

/**
 * Created by cpu10661-local on 9/6/17.
 */

public class Reminder {

    private long mId;
    private long mEventId;
    private int mMinutes;
    private int mMethod;

    public Reminder(int minutes, int method) {
        this.mMinutes = minutes;
        this.mMethod = method;
    }

    public Reminder(long id, long eventId, int minutes, int method) {
        this(minutes, method);
        this.mId = id;
        this.mEventId = eventId;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getEventId() {
        return mEventId;
    }

    public int getMinutes() {
        return mMinutes;
    }

    public int getMethod() {
        return mMethod;
    }
}
