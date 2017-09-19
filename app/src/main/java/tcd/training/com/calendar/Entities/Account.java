package tcd.training.com.calendar.Entities;

/**
 * Created by cpu10661-local on 9/5/17.
 */

public class Account {

    private long mId;
    private String mDisplayName;
    private String mAccountName;
    private String mOwnerAccount;
    private int mColor;
    private boolean mIsPrimary;

    public Account(long id, String displayName, String accountName, String ownerAccount, int color, boolean isPrimary) {
        this.mId = id;
        this.mDisplayName = displayName;
        this.mAccountName = accountName;
        this.mOwnerAccount = ownerAccount;
        this.mColor = color;
        this.mIsPrimary = isPrimary;
    }

    public long getId() {
        return mId;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public String getOwnerAccount() {
        return mOwnerAccount;
    }

    public int getColor() {
        return mColor;
    }

    public boolean isPrimary() {
        return mIsPrimary;
    }
}
