package com.moyersoftware.contender.menu.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moyersoftware.contender.R;
import com.moyersoftware.contender.menu.FriendsFragment;
import com.moyersoftware.contender.menu.data.Friend;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private boolean mSearch = false;
    private final FriendsFragment mFragment;
    private final ArrayList<Friend> mFriends;

    public FriendsAdapter(FriendsFragment fragment, ArrayList<Friend> friends) {
        mFragment = fragment;
        mFriends = friends;
        mSearch = false;
    }

    public FriendsAdapter(FriendsFragment fragment, ArrayList<Friend> friends, boolean search) {
        mFragment = fragment;
        mFriends = friends;
        mSearch = search;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Friend friend = mFriends.get(position);

        holder.nameTxt.setText(friend.getName());
        holder.usernameTxt.setText(friend.getUsername());
        if (!TextUtils.isEmpty(friend.getImage())) {
            Picasso.get().load(friend.getImage())
                    .placeholder(R.drawable.avatar_placeholder)
                    .centerCrop().fit().into(holder.img);
        } else {
            holder.img.setImageResource(R.drawable.avatar_placeholder);
        }
        holder.addImg.setVisibility(mSearch || mFriends.get(position).isIncomingPending()
                ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.friend_img)
        ImageView img;
        @BindView(R.id.friend_name_txt)
        TextView nameTxt;
        @BindView(R.id.friend_username_txt)
        TextView usernameTxt;
        @BindView(R.id.friend_add_img)
        ImageView addImg;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            addImg.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mFriends.get(getAdapterPosition()).isIncomingPending()){
                mFragment.acceptFriend(mFriends.get(getAdapterPosition()).getId());
            } else {
                mFragment.addFriend(mFriends.get(getAdapterPosition()).getId());
            }
        }
    }
}