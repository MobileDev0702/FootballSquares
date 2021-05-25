package com.moyersoftware.contender.menu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.adapter.FriendsAdapter;
import com.moyersoftware.contender.menu.data.Friend;
import com.moyersoftware.contender.menu.data.Friendship;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendsFragment extends Fragment {

    // Constants
    private static final int REQUEST_INVITE = 0;

    // Views
    @BindView(R.id.friends_recycler)
    RecyclerView mFriendsRecycler;
    @BindView(R.id.friends_pending_recycler)
    RecyclerView mFriendsPendingRecycler;
    @BindView(R.id.friends_title_txt)
    TextView mTitleTxt;
    @BindView(R.id.friends_pending_title_txt)
    TextView mPendingTitleTxt;
    @BindView(R.id.friends_find_btn)
    Button mFindBtn;

    // Usual variables
    private final ArrayList<Friend> mFriends = new ArrayList<>();
    private final ArrayList<Friend> mPendingFriends = new ArrayList<>();
    private final ArrayList<Friend> mFoundFriends = new ArrayList<>();
    private String mMyId;
    private DatabaseReference mDatabase;
    private FriendsAdapter mAdapter;
    private FriendsAdapter mFoundAdapter;
    private AlertDialog mSearchDialog;
    private int mCurrentUsernameLength;
    private FriendsAdapter mPendingAdapter;
    private DataSnapshot mDataSnapshot;

    public FriendsFragment() {
        // Required empty public constructor
    }

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);

        initUser();
        initDatabase();
        initRecycler();
        initPendingRecycler();
        initFindBtn();
        loadFriends();

        return view;
    }

    private void initFindBtn() {
        mFindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(),
                        R.style.MaterialDialog);
                @SuppressLint("InflateParams")
                View dialogView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.dialog_friends, null);
                RecyclerView recycler = dialogView.findViewById
                        (R.id.friends_find_recycler);
                recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
                mFoundAdapter = new FriendsAdapter(FriendsFragment.this, mFoundFriends, true);
                recycler.setAdapter(mFoundAdapter);

                final EditText editTxt = dialogView.findViewById(R.id.friends_find_edit_txt);
                editTxt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        searchFriends(editTxt.getText().toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                dialogBuilder.setView(dialogView);
                dialogBuilder.setPositiveButton("Close", null);
                mSearchDialog = dialogBuilder.create();
                mSearchDialog.show();
            }
        });
    }

    private void searchFriends(final String username) {
        mDatabase.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDataSnapshot = dataSnapshot;
                final ArrayList<Friendship> mFriendships = new ArrayList<>();
                for (DataSnapshot friendshipSnapshot : mDataSnapshot.getChildren()) {
                    Friendship friendship = friendshipSnapshot.getValue(Friendship.class);
                    mFriendships.add(friendship);
                }

                mFoundFriends.clear();
                if (!TextUtils.isEmpty(username)) {
                    mDatabase.child("users").addListenerForSingleValueEvent
                            (new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshotUsers) {
                                    mFoundFriends.clear();

                                    for (final DataSnapshot userSnapshot : dataSnapshotUsers.getChildren()) {
                                        try {
                                            final User user = userSnapshot.getValue(User.class);
                                            /*if (user.getName().toLowerCase(Locale.US).contains
                                                    (username.toLowerCase(Locale.US))
                                                    || Util.parseUsername(user.getEmail())
                                                    .toLowerCase(Locale.US).contains(username
                                                            .toLowerCase(Locale.US))
                                                    || Util.parseUsername(user.getName())
                                                    .toLowerCase(Locale.US).contains(username
                                                            .toLowerCase(Locale.US))) {*/
                                                final boolean[] alreadyFriends = {false};
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        for (Friendship friendship : mFriendships) {
                                                            //if ((friendship.getUser1Id().equals(mMyId) && friendship.getUser2Id()
                                                            //        .equals(user.getId())) || (friendship.getUser2Id().equals(mMyId)
                                                            //        && friendship.getUser1Id().equals(user.getId()))) {
                                                            //    alreadyFriends[0] = true;
                                                           // }
                                                        }

                                                        //if (!alreadyFriends[0]) {
                                                            Friend friend = new Friend(userSnapshot.getKey(), user.getName(),
                                                                    Util.parseUsername(user), user.getImage(), user.getEmail(), false);
                                                            if (!mFoundFriends.contains(friend) && !friend.getId().equals(mMyId)) {
                                                                mFoundFriends.add(friend);
                                                                mAdapter.notifyDataSetChanged();
                                                            }
                                                        //}

                                                    }
                                                });
                                            //}
                                        } catch (Exception e) {
                                        }
                                    }

                                    if (TextUtils.isEmpty(username)) {
                                        mFoundFriends.clear();
                                    }
                                    mFoundAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mFoundFriends.clear();
                                    mFoundAdapter.notifyDataSetChanged();
                                }
                            });
                } else {
                    mFoundFriends.clear();
                    mFoundAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadFriends() {
        mDatabase.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFriends.clear();
                mPendingFriends.clear();
                for (DataSnapshot friendshipSnapshot : dataSnapshot.getChildren()) {
                    final Friendship friendship = friendshipSnapshot.getValue(Friendship.class);
                    if (friendship == null || friendship.getUser1Id() == null || friendship.getUser2Id() == null)
                        continue;
                    if (friendship.getUser1Id().equals(mMyId)
                            || friendship.getUser2Id().equals(mMyId)) {
                        String friendId = friendship.getUser1Id().equals(mMyId)
                                ? friendship.getUser2Id() : friendship.getUser1Id();

                        mDatabase.child("users").child(friendId).addListenerForSingleValueEvent
                                (new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);

                                        if (user == null) return;

                                        try {
                                            if (friendship.isPending()) {
                                                if (!friendship.getUser1Id().equals(mMyId)) {
                                                    Friend friend = new Friend(dataSnapshot
                                                            .getKey(), user.getName(),
                                                            user.getUsername(), user.getImage(),
                                                            user.getEmail(), true);
                                                    if (!mPendingFriends.contains(friend)) {
                                                        mPendingFriends.add(friend);
                                                    }
                                                    mPendingAdapter.notifyDataSetChanged();
                                                }
                                            } else {
                                                Friend friend = new Friend(dataSnapshot.getKey(),
                                                        user.getName(), user.getUsername(),
                                                        user.getImage(), user.getEmail(), false);
                                                mFriends.add(friend);
                                                mAdapter.notifyDataSetChanged();
                                            }

                                            mTitleTxt.setVisibility(mFriends.size() > 0 ? View.VISIBLE
                                                    : View.GONE);
                                            mFriendsRecycler.setVisibility(mFriends.size() > 0
                                                    ? View.VISIBLE : View.GONE);
                                            mPendingTitleTxt.setVisibility(mPendingFriends.size() > 0
                                                    ? View.VISIBLE : View.GONE);
                                            mFriendsPendingRecycler.setVisibility(mPendingFriends
                                                    .size() > 0 ? View.VISIBLE : View.GONE);
                                        } catch (Exception e) {

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                    }
                }

                mTitleTxt.setVisibility(mFriends.size() > 0 ? View.VISIBLE
                        : View.GONE);
                mFriendsRecycler.setVisibility(mFriends.size() > 0
                        ? View.VISIBLE : View.GONE);
                mPendingTitleTxt.setVisibility(mPendingFriends.size() > 0
                        ? View.VISIBLE : View.GONE);
                mFriendsPendingRecycler.setVisibility(mPendingFriends
                        .size() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void initUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mMyId = firebaseUser.getUid();
        }
    }

    private void initRecycler() {
        mFriendsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new FriendsAdapter(FriendsFragment.this, mFriends);
        mFriendsRecycler.setAdapter(mAdapter);
    }

    private void initPendingRecycler() {
        mFriendsPendingRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPendingAdapter = new FriendsAdapter(FriendsFragment.this, mPendingFriends);
        mFriendsPendingRecycler.setAdapter(mPendingAdapter);
    }

    public void addFriend(final String userId) {
        mDatabase.child("friends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<ArrayList<Friendship>> t = new GenericTypeIndicator
                                <ArrayList<Friendship>>() {
                        };
                        ArrayList<Friendship> friendships = dataSnapshot.getValue(t);
                        if (friendships == null) {
                            friendships = new ArrayList<>();
                        }
                        friendships.add(new Friendship(mMyId, userId, true));

                        mDatabase.child("friends").setValue(friendships);
                        mSearchDialog.cancel();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public void acceptFriend(final String id) {
        mDatabase.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<Friendship>> t = new GenericTypeIndicator
                        <ArrayList<Friendship>>() {
                };
                ArrayList<Friendship> friendships = dataSnapshot.getValue(t);
                for (int i = 0; i < friendships.size(); i++) {
                    Friendship friendship = friendships.get(i);
                    if (friendship == null) continue;
                    if (friendship.getUser1Id().equals(id) && friendship.getUser2Id()
                            .equals(mMyId)) {
                        friendship.setPending(false);
                        friendships.set(i, friendship);
                        break;
                    }
                }

                mDatabase.child("friends").setValue(friendships);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
