package tcd.training.com.calendar.Entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class Event implements Parcelable{

    private long mId;
    private String mTitle;
    private long mCalendarId;
    private String mLocation;
    private String mDescription;
    private String mTimeZone;

    private long mStartDate;
    private long mEndDate;
    private boolean mAllDay;

    private boolean mHasAlarm;
    private String mRRule;
    private String mDuration;

    private int mDisplayColor;
    private int mAvailability;

    private int mPriority;

    private boolean mIsDirty;
    private boolean mIsDeleted;

    public Event() {}

    public Event(String title, long calendarId, String location, String description, String timeZone,
                 long startDate, long endDate, boolean allDay,
                 boolean hasAlarm, String rRule, String duration,
                 int displayColor, int availability) {
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
        this.mDuration = duration;
        this.mDisplayColor = displayColor;
        this.mAvailability = availability;
    }

    public Event(long id, String title, long calendarId, String location, String description, String timeZone,
                 long startDate, long endDate, boolean allDay,
                 boolean hasAlarm, String rRule, String duration,
                 int displayColor, int availability,
                 boolean isDirty, boolean isDeleted) {
        this(title, calendarId, location, description, timeZone, startDate, endDate, allDay, hasAlarm, rRule, duration, displayColor, availability);
        mId = id;
        mIsDirty = isDirty;
        mIsDeleted = isDeleted;
    }

    public Event(Event event) {
        this(event.getId(),
                event.getTitle(),
                event.getCalendarId(),
                event.getLocation(),
                event.getDescription(),
                event.getTimeZone(),
                event.getStartDate(), event.getEndDate(),
                event.isAllDay(),
                event.hasAlarm(),
                event.getRRule(), event.getDuration(),
                event.getDisplayColor(),
                event.getAvailability(),
                event.isDirty(),
                event.isDeleted()
        );
    }

    protected Event(Parcel in) {
        mId = in.readLong();
        mTitle = in.readString();
        mCalendarId = in.readLong();
        mLocation = in.readString();
        mDescription = in.readString();
        mTimeZone = in.readString();
        mStartDate = in.readLong();
        mEndDate = in.readLong();
        mAllDay = in.readByte() != 0;
        mHasAlarm = in.readByte() != 0;
        mRRule = in.readString();
        mDuration = in.readString();
        mDisplayColor = in.readInt();
        mAvailability = in.readInt();
        mPriority = in.readInt();
        mIsDirty = in.readByte() != 0;
        mIsDeleted = in.readByte() != 0;
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

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public long getCalendarId() {
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

    public void setStartDate(long millis) {
        mStartDate = millis;
    }

    public long getEndDate() {
        return mEndDate;
    }

    public void setEndDate(long millis) {
        mEndDate = millis;
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

    public String getDuration() {
        return mDuration;
    }

    public int getDisplayColor() {
        return mDisplayColor;
    }

    public int getAvailability() {
        return mAvailability;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        mPriority = priority;
    }

    public boolean isDirty() {
        return mIsDirty;
    }

    public boolean isDeleted() {
        return mIsDeleted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mId);
        parcel.writeString(mTitle);
        parcel.writeLong(mCalendarId);
        parcel.writeString(mLocation);
        parcel.writeString(mDescription);
        parcel.writeString(mTimeZone);
        parcel.writeLong(mStartDate);
        parcel.writeLong(mEndDate);
        parcel.writeByte((byte) (mAllDay ? 1 : 0));
        parcel.writeByte((byte) (mHasAlarm ? 1 : 0));
        parcel.writeString(mRRule);
        parcel.writeString(mDuration);
        parcel.writeInt(mDisplayColor);
        parcel.writeInt(mAvailability);
        parcel.writeInt(mPriority);
        parcel.writeByte((byte) (mIsDirty ? 1 : 0));
        parcel.writeByte((byte) (mIsDeleted ? 1 : 0));
    }
}
