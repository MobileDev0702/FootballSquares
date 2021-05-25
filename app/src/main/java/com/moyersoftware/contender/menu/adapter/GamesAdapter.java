package com.moyersoftware.contender.menu.adapter;

import android.annotation.SuppressLint;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.game.data.SelectedSquare;
import com.moyersoftware.contender.menu.GamesFragment;
import com.moyersoftware.contender.menu.MainActivity;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.ViewHolder> {

    private HashMap<String, Event> mEvents;
    private final MainActivity mActivity;
    private final GamesFragment mFragment;
    private final ArrayList<GameInvite.Game> mGames;
    private final String mMyId;
    private Integer mFirstInvitePos;

    public GamesAdapter(MainActivity activity, GamesFragment fragment,
                        ArrayList<GameInvite.Game> games, String myId) {
        mActivity = activity;
        mFragment = fragment;
        mGames = games;
        mMyId = myId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_game, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GameInvite.Game game = mGames.get(position);

        holder.nameTxt.setText(game.getName());

        try {
            if (game.getEventTime() != null) {
                if (game.getEventTime() == -2) {
                    holder.finalLayout.setVisibility(View.VISIBLE);
                    holder.timeTxt.setVisibility(View.GONE);

                    if (((game.getSelectedSquares() == null || (game.getSelectedSquares() != null
                            && game.getSelectedSquares().size() < 100))
                            && (game.getEventTime() == -2 || game.getEventTime()
                            < System.currentTimeMillis())) && !game.allowIncomplete()) {
                        holder.finalTxt.setText("VOID (BOARD ISN'T FILLED)");
                    } else {
                        holder.finalTxt.setText("FINAL");
                    }
                } else if (game.getEventTime() == -1) {
                    holder.finalLayout.setVisibility(View.VISIBLE);
                    holder.timeTxt.setVisibility(View.GONE);

                    holder.finalTxt.setText("LIVE");
                } else {
                    holder.timeTxt.setVisibility(View.VISIBLE);
                    if (!game.isCustom()) {
                        holder.timeTxt.setText(Util.formatDateTime(game.getEventTime()));
                    } else {
                        holder.timeTxt.setText("Custom game board");
                    }
                    holder.finalLayout.setVisibility(View.GONE);

                    if (!TextUtils.isEmpty(game.getInviteName())) {
                        holder.timeTxt.setText(holder.timeTxt.getText() + "\n\uD83C\uDFC8 "
                                + game.getInviteName());
                    }
                }
            }
        } catch (Exception e) {
            Util.Log("Can't display game: " + e);
        }

        boolean invite = !TextUtils.isEmpty(game.getInviteName());
        if (invite) {
            holder.scoreTxt.setText(game.getSquarePrice() + " pts / square");
            holder.mInviteTxt.setText(game.getInviteName() + " has invited you");
            if (mFirstInvitePos == null || position == mFirstInvitePos) {
                holder.itemView.setPadding(0, 24, 0, 0);
                mFirstInvitePos = position;
            }
        } else if (game.isCurrent()) {
            holder.scoreTxt.setText("Current");
            holder.itemView.setPadding(0, 0, 0, 0);
        } else {
            holder.itemView.setPadding(0, 0, 0, 0);
            int mySquaresCount = 0;
            if (game.getSelectedSquares() != null) {
                for (SelectedSquare selectedSquare : game.getSelectedSquares()) {
                    if (selectedSquare.authorId.equals(mMyId)) {
                        mySquaresCount++;
                    }
                }
            }
            holder.scoreTxt.setText(mFragment.getResources().getString(R.string.games_score,
                    mySquaresCount,
                    game.getSelectedSquares() != null ? game.getSelectedSquares().size() : 0));
        }

        try {
            Picasso.get().load(game.getImage()).placeholder
                    (android.R.color.white).centerCrop().fit().placeholder(R.drawable.icon)
                    .into(holder.img);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.icon).centerCrop().fit()
                    .into(holder.img);
        }

        holder.accept.setVisibility(invite ? View.VISIBLE : View.GONE);
        holder.reject.setVisibility(invite ? View.VISIBLE : View.GONE);
        holder.inviteLayout.setVisibility(invite ? View.VISIBLE : View.GONE);
        holder.mDetailsLayout.setVisibility(invite ? View.GONE : View.VISIBLE);
        holder.layout.setClickable(!invite);
        holder.itemView.setClickable(!invite);

        if (mEvents != null) {

            Util.Log(new Gson().toJson(mEvents.get(game.getEventId())));
            holder.teamsTxt.setText(mEvents.get(game.getEventId()).getTeamAway().getAbbrev() + " @ "
                    + mEvents.get(game.getEventId()).getTeamHome().getAbbrev());
        }
    }

    public void resetInvitePos() {
        mFirstInvitePos = null;
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }

    public void setEvents(HashMap<String, Event> events) {
        mEvents = events;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        @BindView(R.id.game_img)
        ImageView img;
        @BindView(R.id.game_name_txt)
        TextView nameTxt;
        @BindView(R.id.game_time_txt)
        TextView timeTxt;
        @BindView(R.id.game_score_txt)
        TextView scoreTxt;
        @BindView(R.id.game_final_layout)
        View finalLayout;
        @BindView(R.id.game_final_txt)
        TextView finalTxt;
        @BindView(R.id.accept)
        View accept;
        @BindView(R.id.reject)
        View reject;
        @BindView(R.id.game_layout)
        View layout;
        @BindView(R.id.game_teams_txt)
        TextView teamsTxt;
        @BindView(R.id.game_details_layout)
        View mDetailsLayout;
        @BindView(R.id.invite_layout)
        View inviteLayout;
        @BindView(R.id.game_invite_txt)
        TextView mInviteTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            layout.setOnClickListener(this);
            try {
                itemView.setClickable(!TextUtils.isEmpty(mGames.get(getAdapterPosition())
                        .getInviteName()));
            } catch (Exception e) {
                // Can't set item clickable
            }
            layout.setOnLongClickListener(this);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.playGame(mGames.get(getAdapterPosition()).getId());
                    removeInvite();
                }
            });
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeInvite();
                }
            });
        }

        private void removeInvite() {
            Util.Log("remove invite: " + mGames.get(getAdapterPosition()).getInviteId());
            FirebaseDatabase.getInstance().getReference().child("game_invites")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(mGames.get(getAdapterPosition()).getInviteId()).removeValue();

            mFragment.initDatabase();
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == -1) return;
            if (TextUtils.isEmpty(mGames.get(getAdapterPosition()).getInviteName())) {
                mFragment.joinGame(mGames.get(getAdapterPosition()).getId());
            } else {
                mActivity.playGame(mGames.get(getAdapterPosition()).getId());
                removeInvite();
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (TextUtils.isEmpty(mGames.get(getAdapterPosition()).getInviteName())) {
                mFragment.deleteGame(mGames.get(getAdapterPosition()));
            }
            return true;
        }
    }
}