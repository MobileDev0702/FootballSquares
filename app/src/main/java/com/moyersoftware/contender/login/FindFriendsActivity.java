package com.moyersoftware.contender.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.login.adapter.FoundFriendsAdapter;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.MainActivity;
import com.moyersoftware.contender.menu.data.Friend;
import com.moyersoftware.contender.menu.data.Friendship;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class FindFriendsActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    // Views
    @BindView(R.id.find_friends_progress_txt)
    TextView mProgressTxt;
    @BindView(R.id.find_friends_recycler)
    RecyclerView mRecycler;
    @BindView(R.id.find_friends_skip_btn)
    Button mFriendsSkipBtn;

    private final ArrayList<User> mUsers = new ArrayList<>();
    private final ArrayList<User> mFoundFriends = new ArrayList<>();
    private FoundFriendsAdapter mPlayersAdapter;
    private String mMyId;
    private final ArrayList<String> mFriendIds = new ArrayList<>();

    // Usual variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        ButterKnife.bind(this);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        overrideActivityAnimation();
        initStatusBar();
        initRecycler();
        initMyAccount();
        loadFriends();
    }

    private void initMyAccount() {
        mMyId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void loadFriends() {
        FirebaseDatabase.getInstance().getReference().child("friends").addListenerForSingleValueEvent
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mFriendIds.clear();
                        for (DataSnapshot friendshipSnapshot : dataSnapshot.getChildren()) {
                            final Friendship friendship = friendshipSnapshot.getValue(Friendship.class);
                            if (friendship == null || friendship.getUser1Id() == null || friendship.getUser2Id() == null)
                                continue;
                            if (friendship.getUser1Id().equals(mMyId)
                                    || friendship.getUser2Id().equals(mMyId)) {
                                final String friendId = friendship.getUser1Id().equals(mMyId)
                                        ? friendship.getUser2Id() : friendship.getUser1Id();

                                FirebaseDatabase.getInstance().getReference().child("users")
                                        .child(friendId).addListenerForSingleValueEvent
                                        (new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                User user = dataSnapshot.getValue(User.class);
                                                if (user!=null) {
                                                    Util.Log("data change: " + user.getName());

                                                    Friend friend = new Friend(dataSnapshot.getKey(),
                                                            user.getName(), user.getUsername(),
                                                            user.getImage(), user.getEmail(), false);
                                                    if (!mFriendIds.contains(friend.getId())) {
                                                        mFriendIds.add(friend.getId());
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });

                                loadUsers();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void initRecycler() {
        //noinspection ConstantConditions
        mPlayersAdapter = new FoundFriendsAdapter(this, mFoundFriends, FirebaseAuth.getInstance()
                .getCurrentUser().getUid());
        mRecycler.setAdapter(mPlayersAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadUsers() {
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mUsers.clear();
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            try {
                                User user = userSnapshot.getValue(User.class);
                                if (!TextUtils.isEmpty(user.getPhone())) {
                                    mUsers.add(user);
                                }
                            } catch (Exception e) {
                                Util.Log("Can't parse user: " + e);
                            }
                        }
                        loadContacts();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void loadContacts() {
        mProgressTxt.setVisibility(View.VISIBLE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            return;
        }

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone
                .CONTENT_URI, new String[]{ContactsContract
                .CommonDataKinds.Phone.NUMBER}, null, null, null);
        if (phones == null) {
            mProgressTxt.setVisibility(View.GONE);
            Toast.makeText(this, "Can't read contacts.", Toast.LENGTH_LONG).show();
            return;
        }
        mFoundFriends.clear();
        mPlayersAdapter.setAlreadyFriends(mFriendIds);
        while (phones.moveToNext()) {
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract
                    .CommonDataKinds.Phone.NUMBER));
            for (User user : mUsers) {
                if (!mFriendIds.contains(user.getId())
                        && PhoneNumberUtils.compare(phoneNumber, user.getPhone())) {
                    mFoundFriends.add(user);
                }
            }
        }
        phones.close();

        if (mFoundFriends.size() == 0) {
            mProgressTxt.setText(R.string.no_friends_found);
        } else {
            mProgressTxt.setVisibility(View.GONE);
        }
        mPlayersAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContacts();
                }
            }
        }
    }

    /**
     * Creates a cross fade effect between loading and registration screens.
     */
    private void overrideActivityAnimation() {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_hold);
    }

    /**
     * Makes the status bar translucent.
     */
    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    public void onSkipButtonClicked(View view) {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    public void updateSkipButton() {
        mFriendsSkipBtn.setText(R.string.btn_continue);
    }

    public void addFriend(final String id) {
        FirebaseDatabase.getInstance().getReference().child("friends")
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
                        friendships.add(new Friendship(mMyId, id, true));

                        FirebaseDatabase.getInstance().getReference().child("friends")
                                .setValue(friendships);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
}