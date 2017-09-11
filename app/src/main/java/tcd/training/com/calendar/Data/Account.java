package tcd.training.com.calendar.Data;

/**
 * Created by cpu10661-local on 9/5/17.
 */

public class Account {

    private int mId;
    private String mDisplayName;
    private String mAccountName;
    private String mOwnerAccount;
    private int mColor;

    public Account(int id, String displayName, String accountName, String ownerAccount) {
        this.mId = id;
        this.mDisplayName = displayName;
        this.mAccountName = accountName;
        this.mOwnerAccount = ownerAccount;
    }

    public int getId() {
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

    public void setColor(int color) {
        this.mColor = color;
    }
}
