package com.moyersoftware.contender.menu.data;

/**
 * Immutable model class for a Player.
 */
public final class PaidPlayer {

    private String userId;
    private boolean paid;

    public PaidPlayer() {
        // Default constructor required for calls to DataSnapshot.getValue(Player.class)
    }

    public PaidPlayer(String userId, Boolean paid) {
        this.userId = userId;
        this.paid = paid;
    }

    public String getUserId() {
        return userId;
    }

    public boolean paid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
