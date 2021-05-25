package com.moyersoftware.contender.game.data;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Immutable model class for a Selected Square.
 */
@IgnoreExtraProperties
public class SelectedSquare {

    public String authorId;
    public String authorName;
    public String authorPhoto;
    public int position;
    public int column;
    public int row;

    public SelectedSquare() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public SelectedSquare(String authorId, String authorName, String authorPhoto, int column,
                          int row, int position) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorPhoto = authorPhoto;
        this.column = column;
        this.row = row;
        this.position = position;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorPhoto() {
        return authorPhoto;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int getPosition() {
        return position;
    }
}
