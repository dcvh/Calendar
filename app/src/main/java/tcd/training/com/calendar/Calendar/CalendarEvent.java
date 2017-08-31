package tcd.training.com.calendar.Calendar;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarEvent {
    private long mId;
    private String mTitle;
    private String mDescription;
    private long mStartDate;
    private long mEndDate;

    public CalendarEvent(long id, String title, String description, long startDate, long endDate) {
        this.mId = id;
        this.mTitle = title;
        this.mDescription = description;
        this.mStartDate = startDate;
        this.mEndDate = endDate;
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public long getStartDate() {
        return mStartDate;
    }

    public long getEndDate() {
        return mEndDate;
    }
}
