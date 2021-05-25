package com.moyersoftware.contender.login.data;

/**
 * Immutable model class for a User.
 */
public final class User {

    private String id;
    private String name;
    private String username;
    private String email;
    private String image;
    private String phone;

    public User() {
    }

    public User(String id, String name, String username, String email, String image, String phone) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.image = image;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }

    public String getPhone() {
        return phone;
    }
}
