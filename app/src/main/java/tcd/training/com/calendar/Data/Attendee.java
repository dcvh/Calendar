package tcd.training.com.calendar.Data;

/**
 * Created by cpu10661-local on 9/6/17.
 */

public class Attendee {

    private long mId;
    private long mEventId;
    private String mName;
    private String mEmail;
    private int mStatus;
    private int mRelationship;

    public Attendee(long id, long eventId, String name, String email, int status, int relationship) {
        this.mId = id;
        this.mEventId = eventId;
        this.mName = name;
        this.mEmail = email;
        this.mStatus = status;
        this.mRelationship = relationship;
    }

    public long getId() {
        return mId;
    }

    public long getEventId() {
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
