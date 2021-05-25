package com.moyersoftware.contender.game.data;

import com.moyersoftware.contender.game.adapter.HostEventsAdapter;

/**
 * Immutable model class for an Event.
 */
public class Event {

    public String id;
    public TeamAway teamAway;
    public TeamHome teamHome;
    public Long time;
    public String timeText;
    public String week;
    public int type = HostEventsAdapter.TYPE_ITEM;
    public boolean custom = false;

    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(Event.class)
    }

    public Event(String id, TeamAway teamAway, TeamHome teamHome, Long time, String timeText,
                 String week, int type, boolean custom) {
        this.id = id;
        this.teamAway = teamAway;
        this.teamHome = teamHome;
        this.time = time;
        this.timeText = timeText;
        this.week = week;
        this.type = type;
        this.custom = custom;
    }

    public Event(String week) {
        this.week = week;
    }

    public String getId() {
        return id;
    }

    public TeamAway getTeamAway() {
        return teamAway;
    }

    public TeamHome getTeamHome() {
        return teamHome;
    }

    public Long getTime() {
        return time;
    }

    public String getTimeText() {
        return timeText;
    }

    public String getWeek() {
        return week;
    }

    public int getType() {
        return type;
    }

    public boolean isCustom() {
        return custom;
    }
}
