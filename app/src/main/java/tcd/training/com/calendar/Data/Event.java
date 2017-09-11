package tcd.training.com.calendar.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class Event implements Parcelable{
    private int mId;
    private String mTitle;
    private int mCalendarId;
    private String mLocation;
    private String mDescription;
    private String mTimeZone;
    private long mStartDate;
    private long mEndDate;
    private boolean mAllDay;
    private boolean mHasAlarm;
    private String mRRule;
    private int mDisplayColor;
    private int mAvailability;

    public Event(String title, int calendarId, String location, String description, String timeZone, long startDate, long endDate,
                 boolean allDay, boolean hasAlarm, String rRule, int displayColor, int availability) {
        this.mTitle = title;
        this.mCalendarId = calendarId;
        this.mLocation = location;
        this.mDescription = description;
        this.mTimeZone = timeZone;
        this.mStartDate = startDate;
        this.mEndDate = endDate;
        this.mAllDay = allDay;
        this.mHasAlarm = hasAlarm;
        this.mRRule = rRule;
        this.mDisplayColor = displayColor;
        this.mAvailability = availability;
    }

    public Event(int id, String title, int calendarId, String location, String description, String timeZone, long startDate, long endDate,
                 boolean allDay, boolean hasAlarm, String rRule, int displayColor, int availability) {
        this(title, calendarId, location, description, timeZone, startDate, endDate, allDay, hasAlarm, rRule, displayColor, availability);
        mId = id;
    }



    protected Event(Parcel in) {
        mId = in.readInt();
        mTitle = in.readString();
        mCalendarId = in.readInt();
        mLocation = in.readString();
        mDescription = in.readString();
        mStartDate = in.readLong();
        mEndDate = in.readLong();
        mAllDay = in.readByte() != 0;
        mHasAlarm = in.readByte() != 0;
        mRRule = in.readString();
        mDisplayColor = in.readInt();
        mAvailability = in.readInt();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getCalendarId() {
        return mCalendarId;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getTimeZone() {
        return mTimeZone;
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

    public boolean hasAlarm() {
        return mHasAlarm;
    }

    public String getRRule() {
        return mRRule;
    }

    public int getDisplayColor() {
        return mDisplayColor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeInt(mId);
        parcel.writeString(mTitle);
        parcel.writeInt(mCalendarId);
        parcel.writeString(mLocation);
        parcel.writeString(mDescription);
        parcel.writeLong(mStartDate);
        parcel.writeLong(mEndDate);
        parcel.writeByte((byte) (mAllDay ? 1 : 0));
        parcel.writeByte((byte) (mHasAlarm ? 1 : 0));
        parcel.writeString(mRRule);
        parcel.writeInt(mDisplayColor);
        parcel.writeInt(mAvailability);
    }
}
