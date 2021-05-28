package com.moyersoftware.contender.menu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.facebook.applinks.AppLinkData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.game.HowToPlayActivity;
import com.moyersoftware.contender.game.HowToUseActivity;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.game.service.WinnerService;
import com.moyersoftware.contender.login.LoginActivity;
import com.moyersoftware.contender.menu.adapter.MainPagerAdapter;
import com.moyersoftware.contender.menu.data.Friendship;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.network.ApiFactory;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends AppCompatActivity {

    // Views
    @BindView(R.id.main_tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.main_pager)
    ViewPager mPager;

    // Usual variables
    private final int[] mTabIcons = new int[]{
            R.drawable.icon_home,
            R.drawable.icon_news,
            R.drawable.icon_settings
    };
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        checkUserInvite();
        checkGameInvite();
        checkFacebookInvite();

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        startWinnerService();
        initPager();
        initTabs();
        initUser();
        //updatePhoneNumber();

        if (!Util.isTutorialShown(mFirebaseUser.getUid())) {
            startActivity(new Intent(this, HowToPlayActivity.class));
        }

        updateGoogleToken();

    }

    private void updateGoogleToken() {
        retrofit2.Call<Void> call = ApiFactory.getApiService().updateToken
                (mFirebaseUser.getUid(), FirebaseInstallations.getInstance().getToken(false).toString());
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call,
                                   retrofit2.Response<Void> response) {
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
            }
        });
    }

    //private void updatePhoneNumber() {
    //    if (ContextCompat.checkSelfPermission(this,
    //            Manifest.permission.READ_PHONE_STATE)
    //            != PackageManager.PERMISSION_GRANTED) {

    //        ActivityCompat.requestPermissions(this,
    //                new String[]{Manifest.permission.READ_PHONE_STATE},
    //                0);
    //        return;
    //    }

    //    try {
    //        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    //        @SuppressLint("HardwareIds") String phoneNumber = tMgr.getLine1Number();

    //        FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseUser.getUid())
    //                .child("phone").setValue(phoneNumber);
    //    }catch (Exception e){
    //        Util.Log("Phone permission is not granted");
    //    }
    //}

   // @Override
    //public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
     //                                      @NonNull int[] grantResults) {
    //    if (grantResults.length > 0
    //            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
    //        updatePhoneNumber();
    //    }
    //}

    private void checkFacebookInvite() {
        AppLinkData.fetchDeferredAppLinkData(this, new AppLinkData.CompletionHandler() {
            @Override
            public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
                final String code;
                if (appLinkData != null) {
                    code = appLinkData.getPromotionCode();
                } else {
                    code = null;
                }
                if (!TextUtils.isEmpty(code)) {
                    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                    database.child("invites").addListenerForSingleValueEvent
                            (new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get all invites
                                    Iterable<DataSnapshot> invites = dataSnapshot.getChildren();
                                    String referralId = null;
                                    for (DataSnapshot invite : invites) {
                                        // Find an invite by code,
                                        // and check if user didn't create it himself
                                        if (invite.getValue(String.class).equals(code)
                                                && !invite.getKey().equals(mFirebaseUser.getUid())) {
                                            referralId = invite.getKey();
                                        }
                                    }

                                    if (referralId == null) {
                                        Toast.makeText(MainActivity.this, "Invite not found",
                                                Toast.LENGTH_SHORT).show();
                                        Util.setReferralCode(null);
                                        return;
                                    }

                                    final Friendship friendship = new Friendship(referralId,
                                            mFirebaseUser.getUid(), false);
                                    addFriend(database, friendship, referralId);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                }
            }
        });
    }

    private void startWinnerService() {
        startService(new Intent(this, WinnerService.class));
    }

    private void initUser() {
        FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseUser.getUid())
                .child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.getValue(String.class);
                    Util.setDisplayName(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkUserInvite() {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final String code = Util.getReferralCode();
        if (!TextUtils.isEmpty(code)) {
            database.child("invites").addListenerForSingleValueEvent
                    (new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get all invites
                            Iterable<DataSnapshot> invites = dataSnapshot.getChildren();
                            String referralId = null;
                            for (DataSnapshot invite : invites) {
                                // Find an invite by code,
                                // and check if user didn't create it himself
                                if (invite.getValue(String.class).equals(code) && !invite.getKey()
                                        .equals(mFirebaseUser.getUid())) {
                                    referralId = invite.getKey();
                                }
                            }

                            if (referralId == null) {
                                Toast.makeText(MainActivity.this, "Invite not found",
                                        Toast.LENGTH_SHORT).show();
                                Util.setReferralCode(null);
                                return;
                            }

                            final Friendship friendship = new Friendship(referralId,
                                    mFirebaseUser.getUid(), false);
                            addFriend(database, friendship, referralId);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }
    }

    private void addFriend(final DatabaseReference database, final Friendship friendship,
                           final String referralId) {
        // Get all friends
        database.child("friends").addListenerForSingleValueEvent
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Check if the referral and current user
                        // aren't already friends
                        GenericTypeIndicator<ArrayList<Friendship>> t = new GenericTypeIndicator
                                <ArrayList<Friendship>>() {
                        };
                        ArrayList<Friendship> friendships = dataSnapshot.getValue(t);
                        if (friendships == null) {
                            friendships = new ArrayList<>();
                        }
                        boolean alreadyFriends = false;
                        for (Friendship friendship : friendships) {
                            if (friendship != null && ((friendship.getUser1Id().equals
                                    (mFirebaseUser.getUid()) && friendship.getUser2Id().equals
                                    (referralId)) || (friendship.getUser2Id().equals(mFirebaseUser
                                    .getUid()) && friendship.getUser1Id().equals(referralId)))) {
                                alreadyFriends = true;
                                break;
                            }
                        }

                        if (!alreadyFriends) {
                            friendships.add(friendship);
                            database.child("friends").setValue(friendships);
                        }

                        Util.setReferralCode(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Util.setCurrentPlayerId(FirebaseAuth.getInstance()
                .getCurrentUser().getUid());
    }

    private void checkGameInvite() {
        String data = getIntent().getDataString();

        if (!TextUtils.isEmpty(data) && data.contains("moyersoftware.com/contender#")) {
            String gameId = data.substring(data.indexOf("#") + 1);
            playGame(gameId);
        }
    }

    public void playGame(final String gameId) {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Retrieve current list of players for the game user wants to join
        retrofit2.Call<GameInvite.Game> call = ApiFactory.getApiService().getGame(gameId);
        call.enqueue(new retrofit2.Callback<GameInvite.Game>() {
            @Override
            public void onResponse(retrofit2.Call<GameInvite.Game> call,
                                   retrofit2.Response<GameInvite.Game> response) {
                if (!response.isSuccessful()) finish();

                GameInvite.Game game = response.body();

                if (firebaseUser != null) {
                    ArrayList<Player> players = game.getPlayers();
                    if (players == null) players = new ArrayList<>();

                    if (!game.getAuthor().getUserId().equals(firebaseUser.getUid())
                            && !players.contains(new Player(firebaseUser.getUid(), null,
                            firebaseUser.getEmail(), Util.getDisplayName(),
                            Util.getPhoto()))) {
                        players.add(new Player(firebaseUser.getUid(), null,
                                firebaseUser.getEmail(), Util.getDisplayName(),
                                Util.getPhoto()));

                        game.setPlayers(players);
                        updateGameOnServer(game);
                    } else {
                        startActivity(new Intent(MainActivity.this, GameBoardActivity.class)
                                .putExtra(GameBoardActivity.EXTRA_GAME_ID, gameId));
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<GameInvite.Game> call, Throwable t) {
            }
        });
    }

    private void updateGameOnServer(final GameInvite.Game game) {
        retrofit2.Call<Void> call = ApiFactory.getApiService().updateGame(game);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call,
                                   retrofit2.Response<Void> response) {
                startActivity(new Intent(MainActivity.this, GameBoardActivity.class)
                        .putExtra(GameBoardActivity.EXTRA_GAME_ID, game.getId()));
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
            }
        });
    }

    private void initPager() {
        mPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
    }

    private void initTabs() {
        mTabLayout.setupWithViewPager(mPager);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            //noinspection ConstantConditions
            mTabLayout.getTabAt(i).setIcon(mTabIcons[i]);
        }
    }

    /**
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    public void onLogOutButtonClicked(View view) {
        mFirebaseAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void onSupportButtonClicked(View view) {
        new AlertDialog.Builder(this, R.style.MaterialDialog)
                .setTitle("Select topic")
                .setSingleChoiceItems(new String[]{
                        "Support Issue",
                        "Report Abuse",
                        "Suggestions",
                        "Other"
                }, 0, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{Util.SUPPORT_EMAIL});
                        String subject;
                        if (selectedPosition == 0) {
                            subject = "Support Issue (Android Squares)";
                        } else if (selectedPosition == 1) {
                            subject = "Report Abuse (Android Squares)";
                        } else if (selectedPosition == 2) {
                            subject = "Suggestions (Android Squares)";
                        } else {
                            subject = "Feedback (Android Squares)";
                        }
                        i.putExtra(Intent.EXTRA_SUBJECT, subject);
                        try {
                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(MainActivity.this, "There are no email clients installed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();


    }

    public void onRateButtonClicked(View view) {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                    + appPackageName)));
        } catch (android.content.ActivityNotFoundException exception) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                    ("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public void onAboutButtonClicked(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        dialogBuilder.setTitle("");
        @SuppressLint("InflateParams")
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_about, null);

        // Show app version
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0)
                    .versionName;
            ((TextView) dialogView.findViewById(R.id.aboutVersionTxt)).setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Util.Log("Can't get app version");
        }

        dialogBuilder.setView(dialogView);
        dialogBuilder.setNegativeButton("OK", null);
        dialogBuilder.create().show();
    }

    public void onHowToButtonClicked(View view) {
        startActivity(new Intent(this, HowToPlayActivity.class));
    }

    public void onHowToUseButtonClicked(View view) {
        startActivity(new Intent(this, HowToUseActivity.class));
    }
}
