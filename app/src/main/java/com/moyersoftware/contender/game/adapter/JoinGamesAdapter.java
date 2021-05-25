package com.moyersoftware.contender.game.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.JoinActivity;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class JoinGamesAdapter extends RecyclerView.Adapter<JoinGamesAdapter.ViewHolder> {

    private final String mMyId;
    private final String mMyEmail;
    private final String mMyName;
    private final String mMyPhoto;
    private final JoinActivity mActivity;
    private ArrayList<GameInvite.Game> mGames;

    public JoinGamesAdapter(JoinActivity activity, ArrayList<GameInvite.Game> games, String myId,
                            String myEmail, String myName, String myPhoto) {
        mActivity = activity;
        mGames = games;
        mMyId = myId;
        mMyEmail = myEmail;
        mMyName = myName;
        mMyPhoto = myPhoto;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game_join, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GameInvite.Game game = mGames.get(position);

        try {

            holder.nameTxt.setText(game.getName());
            holder.authorTxt.setText("Created by "+game.getAuthor().getName());
            Picasso.get().load(game.getImage()).placeholder(R.drawable.icon)
                    .centerCrop().fit().into(holder.img);

            if (game.getPlayers() != null && game.getPlayers().contains(new Player(mMyId, null,
                    mMyEmail, mMyName, mMyPhoto))) {
                holder.joinBtn.setText(R.string.join_disabled_btn);
            } else {
                holder.joinBtn.setText(R.string.join_btn);
            }
        } catch (Exception e) {
            Util.Log("can't show game");
        }
    }

    @Override
    public int getItemCount() {
        Util.Log("games size: " + mGames.size());
        return mGames.size();
    }

    public void setGames(ArrayList<GameInvite.Game> games) {
        mGames = games;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.game_img)
        ImageView img;
        @BindView(R.id.game_name_txt)
        TextView nameTxt;
        @BindView(R.id.game_author_txt)
        TextView authorTxt;
        @BindView(R.id.game_join_btn)
        Button joinBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            joinBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            GameInvite.Game game = mGames.get(getAdapterPosition());
            // Check if user already joined this game before
            if (game.getPlayers() != null && game.getPlayers().contains(new Player(mMyId, null,
                    mMyEmail, mMyName, mMyPhoto))) {
                // Open the game board screen
                mActivity.playGame(game.getId());
            } else {
                // Ask for password
                mActivity.joinGame(game.getId(), game.getPassword());
            }
        }
    }
}