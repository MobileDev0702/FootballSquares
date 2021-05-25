package com.moyersoftware.contender.game.data;

import com.moyersoftware.contender.menu.data.Player;

/**
 * Immutable model class for a Winner.
 */
public final class Winner {

    private Player player;
    private boolean consumed;

    public Winner() {
        // Default constructor required for calls to DataSnapshot.getValue(Winner.class)
    }

    public Winner(Player player, boolean consumed) {
        this.player = player;
        this.consumed = consumed;
    }

    public Player getPlayer() {
        return player   ;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
}
