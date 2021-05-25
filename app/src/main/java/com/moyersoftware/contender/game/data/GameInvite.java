package com.moyersoftware.contender.game.data;

import androidx.annotation.Nullable;

import com.moyersoftware.contender.menu.data.PaidPlayer;
import com.moyersoftware.contender.menu.data.Player;

import java.util.ArrayList;

/**
 * Immutable model class for a Game Invite.
 */
public class GameInvite {

    public String name;
    public Game game;

    public GameInvite() {
        // Default constructor required for calls to DataSnapshot.getValue(GameInvite.class)
    }

    public GameInvite(String name, Game game) {
        this.name = name;
        this.game = game;
    }

    public String getName() {
        return name;
    }

    public Game getGame() {
        return game;
    }

    public static class Game {

        public String eventId;
        public String id;
        public String name;
        public long time;
        public String image;
        public String score;
        public Player author;
        public String password;
        public int squarePrice;
        public int quarter1Price;
        public int quarter2Price;
        public int quarter3Price;
        public int finalPrice;
        public int totalPrice;
        public double latitude;
        public double longitude;
        public ArrayList<Player> players;
        public ArrayList<PaidPlayer> paidPlayers;
        public ArrayList<Integer> rowNumbers;
        public ArrayList<Integer> columnNumbers;
        public ArrayList<SelectedSquare> selectedSquares;
        public Winner quarter1Winner;
        public Winner quarter2Winner;
        public Winner quarter3Winner;
        public Winner finalWinner;
        public boolean current;
        public String currentQuarter;
        public String code;
        public String rules;
        @Nullable
        public Long eventTime;
        @Nullable
        public String inviteName;
        @Nullable
        public String inviteId;
        @Nullable
        public int squaresLimit;
        public boolean custom;
        public boolean allow_incomplete;

        public Game() {
            // Default constructor required for calls to DataSnapshot.getValue(Game.class)
        }

        public Game(String eventId, String id, String name, Long time, String image, String score,
                    Player author, String password, int squarePrice,
                    int quarter1Price, int quarter2Price, int quarter3Price, int finalPrice,
                    int totalPrice, double latitude, double longitude, ArrayList<Player> players,
                    ArrayList<PaidPlayer> paidPlayers, ArrayList<Integer> rowNumbers,
                    ArrayList<Integer> columnNumbers, ArrayList<SelectedSquare> selectedSquares,
                    Winner quarter1Winner, Winner quarter2Winner, Winner quarter3Winner,
                    Winner finalWinner, boolean current, String currentQuarter, String code,
                    String rules, int squaresLimit, boolean custom, boolean allow_incomplete) {
            this.eventId = eventId;
            this.id = id;
            this.name = name;
            this.time = time;
            this.image = image;
            this.score = score;
            this.author = author;
            this.password = password;
            this.squarePrice = squarePrice;
            this.quarter1Price = quarter1Price;
            this.quarter2Price = quarter2Price;
            this.quarter3Price = quarter3Price;
            this.finalPrice = finalPrice;
            this.totalPrice = totalPrice;
            this.latitude = latitude;
            this.longitude = longitude;
            this.paidPlayers = paidPlayers;
            this.players = players;
            this.rowNumbers = rowNumbers;
            this.columnNumbers = columnNumbers;
            this.selectedSquares = selectedSquares;
            this.quarter1Winner = quarter1Winner;
            this.quarter2Winner = quarter2Winner;
            this.quarter3Winner = quarter3Winner;
            this.finalWinner = finalWinner;
            this.current = current;
            this.currentQuarter = currentQuarter;
            this.code = code;
            this.rules = rules;
            this.squaresLimit = squaresLimit;
            this.custom = custom;
            this.allow_incomplete = allow_incomplete;
        }

        public String getEventId() {
            return eventId;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public long getTime() {
            return time;
        }

        public String getImage() {
            return image;
        }

        public String getScore() {
            return score;
        }

        public Player getAuthor() {
            return author;
        }

        public String getPassword() {
            return password;
        }

        public int getSquarePrice() {
            return squarePrice;
        }

        public int getQuarter1Price() {
            return quarter1Price;
        }

        public int getQuarter2Price() {
            return quarter2Price;
        }

        public int getQuarter3Price() {
            return quarter3Price;
        }

        public int getFinalPrice() {
            return finalPrice;
        }

        public int getTotalPrice() {
            return totalPrice;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public ArrayList<Player> getPlayers() {
            return players;
        }

        public ArrayList<PaidPlayer> getPaidPlayers() {
            return paidPlayers;
        }

        public ArrayList<Integer> getRowNumbers() {
            return rowNumbers;
        }

        public ArrayList<Integer> getColumnNumbers() {
            return columnNumbers;
        }

        public ArrayList<SelectedSquare> getSelectedSquares() {
            return selectedSquares;
        }

        public Winner getQuarter1Winner() {
            return quarter1Winner;
        }

        public Winner getQuarter2Winner() {
            return quarter2Winner;
        }

        public Winner getQuarter3Winner() {
            return quarter3Winner;
        }

        public Winner getFinalWinner() {
            return finalWinner;
        }

        public boolean isCurrent() {
            return current;
        }

        public String getCurrentQuarter() {
            return currentQuarter;
        }

        public String getCode() {
            return code;
        }

        public String getRules() {
            return rules;
        }

        public Long getEventTime() {
            return eventTime;
        }

        public void setEventTime(long eventTime) {
            this.eventTime = eventTime;
        }

        public String getInviteName() {
            return inviteName;
        }

        public void setInviteName(String inviteName) {
            this.inviteName = inviteName;
        }

        public String getInviteId() {
            return inviteId;
        }

        public void setInviteId(String inviteId) {
            this.inviteId = inviteId;
        }

        public int getSquaresLimit() {
            return squaresLimit;
        }

        public boolean isCustom() {
            return custom;
        }

        public boolean allowIncomplete() {
            return allow_incomplete;
        }

        public void setSelectedSquares(ArrayList<SelectedSquare> selectedSquares) {
            this.selectedSquares = selectedSquares;
        }

        public void setPlayers(ArrayList<Player> players) {
            this.players = players;
        }

        public void setPaidPlayers(ArrayList<PaidPlayer> paidPlayers) {
            this.paidPlayers = paidPlayers;
        }

        public String getInviteIdCompare() {
            return inviteId == null ? "0" : inviteId;
        }
    }
}

