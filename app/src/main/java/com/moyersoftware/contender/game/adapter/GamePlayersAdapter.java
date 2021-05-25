package com.moyersoftware.contender.game.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.menu.data.Player;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GamePlayersAdapter extends RecyclerView.Adapter<GamePlayersAdapter.ViewHolder> {

    // Usual variables
    private final GameBoardActivity mActivity;
    private ArrayList<Player> mPlayers = new ArrayList<>();
    private String mMyId;

    public GamePlayersAdapter(GameBoardActivity activity, ArrayList<Player> players, String myId) {
        mActivity = activity;
        mPlayers = players;
        mMyId = myId;
    }

    public void setCurrentPlayerId(String playerId) {
        mMyId = playerId;
        try {
            notifyDataSetChanged();
        } catch (Exception e) {
            // Can't update list
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_player, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Player player = mPlayers.get(position);
        holder.nameTxt.setText(player.getName());
        holder.radioButton.setChecked(player.getUserId().equals(mMyId));
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            CompoundButton.OnCheckedChangeListener {

        @BindView(R.id.player_name_txt)
        TextView nameTxt;
        @BindView(R.id.player_radio_btn)
        RadioButton radioButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            radioButton.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View v) {
            mActivity.selectPlayer(mPlayers.get(getAdapterPosition()));
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                try {
                    mActivity.selectPlayer(mPlayers.get(getAdapterPosition()));
                } catch (Exception e) {
                    // Adapter position is -1
                }
            }
        }
    }
}