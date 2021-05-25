package com.moyersoftware.contender.menu.data;

/**
 * Immutable model class for a Friend.
 */
public final class Friend {

    private final String mId;
    private final String mName;
    private final String mUsername;
    private final String mImage;
    private final String mEmail;
    private boolean mIncomingPending = false;

    public Friend(String id, String name, String username, String image, String email,
                  boolean incomingPending) {
        mId = id;
        mName = name;
        mUsername = username;
        mImage = image;
        mEmail = email;
        mIncomingPending = incomingPending;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getImage() {
        return mImage;
    }

    public String getEmail() {
        return mEmail;
    }

    public Boolean isIncomingPending() {
        return mIncomingPending;
    }
}
