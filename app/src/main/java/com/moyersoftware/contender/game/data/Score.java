package com.moyersoftware.contender.game.data;

/**
 * Immutable model class for a Score.
 */
public class Score {

    public String q1;
    public String q2;
    public String q3;
    public String q4;
    public String total;

    public Score() {
        // Default constructor required for calls to DataSnapshot.getValue(Game.class)
    }

    public Score(String q1, String q2, String q3, String q4, String total) {
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
        this.total = total;
    }

    public String getQ1() {
        return q1;
    }

    public String getQ2() {
        return q2;
    }

    public String getQ3() {
        return q3;
    }

    public String getQ4() {
        return q4;
    }

    public String getTotal() {
        return total;
    }
}
