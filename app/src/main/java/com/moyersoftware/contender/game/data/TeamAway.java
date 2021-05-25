package com.moyersoftware.contender.game.data;

/**
 * Immutable model class for an Away Team.
 */
public class TeamAway {

    public String image;
    public String name;
    public Score score;
    public String abbrev;
    public String color;
    public String colorOff;
    public String font;
    public String fontoff;
    public String location;
    public String img;

    public TeamAway() {
        // Default constructor required for calls to DataSnapshot.getValue(Game.class)
    }

    public TeamAway(String image, String name, Score score, String abbrev, String color,
                    String location, String img, String colorOff, String font, String fontOff) {
        this.image = image;
        this.name = name;
        this.score = score;
        this.abbrev = abbrev;
        this.location = location;
        this.img = img;
        this.color = color;
        this.colorOff = colorOff;
        this.font = font;
        this.fontoff = fontOff;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public Score getScore() {
        return score;
    }

    public String getAbbrev() {
        return abbrev;
    }

    public String getLocation() {
        return location;
    }

    public String getImg() {
        return img;
    }

    public String getColor() {
        return color;
    }

    public String getColorOff() {
        return colorOff;
    }

    public String getFont() {
        return font;
    }

    public String getFontOff() {
        return fontoff;
    }
}
