package com.moyersoftware.contender.game.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.menu.data.Player;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GameFriendsSquaresAdapter extends RecyclerView.Adapter<GameFriendsSquaresAdapter.ViewHolder> {

    // Usual variables
    private ArrayList<Player> mPlayers = new ArrayList<>();
    private ArrayList<Integer> mSelectedSquaresCount = new ArrayList<>();

    public GameFriendsSquaresAdapter(ArrayList<Player> players,
                                     ArrayList<Integer> selectedSquaresCount) {
        mPlayers = players;
        mSelectedSquaresCount = selectedSquaresCount;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_invite_friend_live, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Player player = mPlayers.get(position);
        holder.nameTxt.setText(player.getName());
        holder.squaresTxt.setText("◻ " + mSelectedSquaresCount.get(position) + " selected");
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.friend_invite_name_txt)
        TextView nameTxt;
        @BindView(R.id.friend_squares_txt)
        TextView squaresTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}