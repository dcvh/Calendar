package tcd.training.com.calendar.Calendar;

/**
 * Created by cpu10661-local on 9/6/17.
 */

public class Attendee {

    private int mId;
    private int mEventId;
    private String mName;
    private String mEmail;
    private int mStatus;
    private int mRelationship;

    public Attendee(int id, int eventId, String name, String email, int status, int relationship) {
        this.mId = id;
        this.mEventId = eventId;
        this.mName = name;
        this.mEmail = email;
        this.mStatus = status;
        this.mRelationship = relationship;
    }

    public int getId() {
        return mId;
    }

    public int getEventId() {
        return mEventId;
    }

    public String getName() {
        return mName;
    }

    public String getEmail() {
        return mEmail;
    }

    public int getStatus() {
        return mStatus;
    }

    public int getRelationship() {
        return mRelationship;
    }
}
