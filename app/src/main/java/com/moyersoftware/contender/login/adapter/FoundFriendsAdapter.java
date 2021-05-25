package com.moyersoftware.contender.login.adapter;

import android.annotation.SuppressLint;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.login.FindFriendsActivity;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FoundFriendsAdapter extends RecyclerView.Adapter<FoundFriendsAdapter.ViewHolder> {

    // Usual variables
    private final FindFriendsActivity mActivity;
    private ArrayList<User> mUsers = new ArrayList<>();
    private String mMyId;
    private ArrayList<String> mAlreadyFriends;

    public FoundFriendsAdapter(FindFriendsActivity activity, ArrayList<User> users, String myId) {
        mActivity = activity;
        mUsers = users;
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
                (R.layout.item_found_friend, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.nameTxt.setText(user.getName());
        if (!TextUtils.isEmpty(user.getUsername())) {
            holder.usernameTxt.setText("@" + user.getUsername());
        } else {
            holder.usernameTxt.setText("@" + Util.parseUsername(user));
        }
        if (!TextUtils.isEmpty(user.getImage())) {
            Picasso.get().load(user.getImage()).fit().centerCrop().into(holder.img);
        } else {
            holder.img.setImageResource(R.drawable.avatar_placeholder);
        }
        holder.addImg.setImageResource(mAlreadyFriends.contains(user.getId())
                ? R.drawable.added_friend : R.drawable.add_friend);
        holder.addImg.setEnabled(!mAlreadyFriends.contains(user.getId()));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setAlreadyFriends(ArrayList<String> alreadyFriends) {
        mAlreadyFriends = alreadyFriends;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.player_name_txt)
        TextView nameTxt;
        @BindView(R.id.player_username_txt)
        TextView usernameTxt;
        @BindView(R.id.player_img)
        ImageView img;
        @BindView(R.id.player_add_img)
        ImageView addImg;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            addImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAlreadyFriends.add(mUsers.get(getAdapterPosition()).getId());
                    mActivity.updateSkipButton();
                    mActivity.addFriend(mUsers.get(getAdapterPosition()).getId());
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }
}