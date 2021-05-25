package com.moyersoftware.contender.menu.data;

/**
 * Immutable model class for an Invite.
 */
public final class Invite {

    private final String mUserId;
    private final String mCode;

    public Invite(String userId, String code) {
        mUserId = userId;
        mCode = code;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getCode() {
        return mCode;
    }
}
