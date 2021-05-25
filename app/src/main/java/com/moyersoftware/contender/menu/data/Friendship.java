package com.moyersoftware.contender.menu.data;

/**
 * Immutable model class for a Friendship.
 */
public final class Friendship {

    private String user1Id;
    private String user2Id;
    private Boolean pending = false;

    public Friendship(String user1Id, String user2Id, Boolean pending) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.pending = pending;
    }

    public Friendship() {
        // Default constructor required for calls to DataSnapshot.getValue(Friendship.class)
    }

    public String getUser1Id() {
        return user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public Boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }
}
