package com.moyersoftware.contender.menu.data;

/**
 * Immutable model class for a Player.
 */
public final class PaidPlayer {

    private String userId;
    private boolean paid;
    private double totalPaid;

    public PaidPlayer() {
        // Default constructor required for calls to DataSnapshot.getValue(Player.class)
    }

    public PaidPlayer(String userId, Boolean paid, double totalPaid) {
        this.userId = userId;
        this.paid = paid;
        this.totalPaid = totalPaid;
    }

    public String getUserId() {
        return userId;
    }

    public boolean paid() {
        return paid;
    }

    public double getTotalPaid() { return totalPaid; }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }
}
