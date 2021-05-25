package com.moyersoftware.contender.menu.data;

/**
 * Immutable model class for a Player.
 */
public final class Player {

    private String userId;
    private String createdByUserId;
    private String email;
    private String name;
    private String photo;

    public Player() {
        // Default constructor required for calls to DataSnapshot.getValue(Player.class)
    }

    public Player(String userId, String createdByUserId, String email, String name, String photo) {
        this.userId = userId;
        this.createdByUserId = createdByUserId;
        this.email = email;
        this.name = name;
        this.photo = photo;
    }

    public String getUserId() {
        return userId;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhoto() {
        return photo;
    }

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;
        if (object != null && object instanceof Player) {
            sameSame = this.userId.equals(((Player) object).userId);
        }
        return sameSame;
    }
}
