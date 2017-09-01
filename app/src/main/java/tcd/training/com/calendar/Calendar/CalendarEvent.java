package tcd.training.com.calendar.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarEvent implements Parcelable{
    private long mId;
    private String mTitle;
    private String mOrganizer;
    private String mLocation;
    private String mDescription;
    private long mStartDate;
    private long mEndDate;
    private boolean mAllDay;

    public CalendarEvent(long id, String title, String organizer, String location,
                         String description, long startDate, long endDate, boolean allDay) {
        this.mId = id;
        this.mTitle = title;
        this.mOrganizer = organizer;
        this.mLocation = location;
        this.mDescription = description;
        this.mStartDate = startDate;
        this.mEndDate = endDate;
        this.mAllDay = allDay;
    }

    protected CalendarEvent(Parcel in) {
        mId = in.readLong();
        mTitle = in.readString();
        mOrganizer = in.readString();
        mLocation = in.readString();
        mDescription = in.readString();
        mStartDate = in.readLong();
        mEndDate = in.readLong();
        mAllDay = in.readByte() != 0;
    }

    public static final Creator<CalendarEvent> CREATOR = new Creator<CalendarEvent>() {
        @Override
        public CalendarEvent createFromParcel(Parcel in) {
            return new CalendarEvent(in);
        }

        @Override
        public CalendarEvent[] newArray(int size) {
            return new CalendarEvent[size];
        }
    };

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getOrganizer() {
        return mOrganizer;
    }

    public String getLocation() {
        return mLocation;
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

    public boolean isAllDay() {
        return mAllDay;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mId);
        parcel.writeString(mTitle);
        parcel.writeString(mOrganizer);
        parcel.writeString(mLocation);
        parcel.writeString(mDescription);
        parcel.writeLong(mStartDate);
        parcel.writeLong(mEndDate);
        parcel.writeByte((byte) (mAllDay ? 1 : 0));
    }
}
