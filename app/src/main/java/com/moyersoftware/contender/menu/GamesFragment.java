package com.moyersoftware.contender.menu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.game.HostActivity;
import com.moyersoftware.contender.game.HowToPlayActivity;
import com.moyersoftware.contender.game.HowToUseActivity;
import com.moyersoftware.contender.game.JoinActivity;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.game.data.SelectedSquare;
import com.moyersoftware.contender.game.service.firebase.MyFirebaseMessagingService;
import com.moyersoftware.contender.menu.adapter.GamesAdapter;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.network.ApiFactory;
import com.moyersoftware.contender.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.moyersoftware.contender.util.MyApplication.getContext;

public class GamesFragment extends Fragment {

    // Views
    @BindView(R.id.games_recycler)
    RecyclerView mGamesRecycler;
    //@BindView(R.id.games_host_btn)
    //Button mHostBtn;
    //@BindView(R.id.games_join_btn)
    //Button mJoinBtn;
    @BindView(R.id.games_title_txt)
    TextView mTitleTxt;
    //@BindView(R.id.games_how_to_use_btn)
    //Button mHowToUseBtn;
    //@BindView(R.id.games_how_to_btn)
    //Button mHowToBtn;
    //@BindView(R.id.welcome_close_img)
    //View mCloseImg;
    //@BindView(R.id.welcome_layout)
    //View mWelcomeLayout;
    @BindView(R.id.btn_fab)
    FloatingActionButton fab;
    private PopupWindow popupWindow;

    // Usual variables
    private final ArrayList<GameInvite.Game> mGames = new ArrayList<>();
    private GamesAdapter mAdapter;
    private final HashMap<String, Long> mEventTimes = new HashMap<>();
    private String mGameToRemoveId;
    private FirebaseUser mFirebaseUser;
    private GameInvite.Game mRemoveGame;
    private DatabaseReference mDatabase;

    public GamesFragment() {
        // Required empty public constructor
    }

    public static GamesFragment newInstance() {
        return new GamesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);
        ButterKnife.bind(this, view);

        initDatabase();
        initRecycler();
        initButtons();
        //initWelcomeLayout();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadGames();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerRealTimeListener();
    }

    private void registerRealTimeListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyFirebaseMessagingService.TYPE_GAMES_UPDATED);
        filter.addAction(MyFirebaseMessagingService.TYPE_EVENTS_UPDATED);
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadGames();
            }
        }, filter);
    }

    private void loadGames() {
        Util.Log("Load games1");
        retrofit2.Call<ArrayList<Event>> call = ApiFactory.getApiService().getEvents();
        call.enqueue(new retrofit2.Callback<ArrayList<Event>>() {
            @Override
            public void onResponse(retrofit2.Call<ArrayList<Event>> call,
                                   retrofit2.Response<ArrayList<Event>> response) {
                Util.Log("Load games 2");
                if (!response.isSuccessful()) return;

                mEventTimes.clear();
                HashMap<String, Event> eventsMap = new HashMap<>();
                ArrayList<Event> events = response.body();
                Util.Log("Load games 3");
                for (Event event : events) {
                    if (event.getTime() > 0) {
                        mEventTimes.put(event.getId(), event.getTime());
                    } else {
                        mEventTimes.put(event.getId(), event.getTime());
                    }
                    eventsMap.put(event.getId(), event);
                }
                mAdapter.setEvents(eventsMap);
                Util.Log("Load games 4 = " + events.size());

                getGames(mDatabase, mFirebaseUser);
            }

            @Override
            public void onFailure(retrofit2.Call<ArrayList<Event>> call, Throwable t) {
            }
        });
    }

    //private void initWelcomeLayout() {
    //    if (Util.showWelcomeBanner(mFirebaseUser.getUid()))
    //        mWelcomeLayout.setVisibility(View.VISIBLE);
    //    mCloseImg.setOnClickListener(new View.OnClickListener() {
    //        @Override
     //       public void onClick(View view) {
    //            mWelcomeLayout.setVisibility(View.GONE);
    //            Util.hideWelcomeBanner(mFirebaseUser.getUid());
    //        }
    //    });
    //}

    public void initDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = firebaseAuth.getCurrentUser();
    }

    private void getGames(final DatabaseReference database, final FirebaseUser firebaseUser) {
        retrofit2.Call<ArrayList<GameInvite.Game>> call = ApiFactory.getApiService().getGames();
        call.enqueue(new retrofit2.Callback<ArrayList<GameInvite.Game>>() {
            @Override
            public void onResponse(retrofit2.Call<ArrayList<GameInvite.Game>> call,
                                   retrofit2.Response<ArrayList<GameInvite.Game>> response) {
                Util.Log("Check game 0");
                if (!response.isSuccessful()) return;


                Util.Log("Check game 0.5");


                Util.Log("Response, " + response.body().toString());
                // Update the games list
                mAdapter.resetInvitePos();
                mGames.clear();
                for (final GameInvite.Game game : response.body()) {
                    Util.Log("Check game 1");
                    Util.Log(game.toString());
                    try {
                        if (game != null && game.getAuthor() != null && (game.getAuthor().getUserId().equals(firebaseUser
                                .getUid()) || (game.getPlayers() != null && game.getPlayers()
                                .contains(new Player(firebaseUser.getUid(), null,
                                        firebaseUser.getEmail(),
                                        Util.getDisplayName(), Util.getPhoto()))))) {
                            Util.Log("Check game 2 "+game.toString());

                            game.setEventTime(mEventTimes.get(game.getEventId()));
                            if (!mGames.contains(game)) {
                                mGames.add(game);
                            }
                            Util.Log("Check game 3");
                            if ((game.getSelectedSquares() == null || (game.getSelectedSquares() != null
                                    && game.getSelectedSquares().size() < 100))
                                    && (mEventTimes.get(game.getEventId()) == -2
                                    || mEventTimes.get(game.getEventId()) < System.currentTimeMillis())) {
                                String code = game.getCode();
                                if (!TextUtils.isEmpty(code)) {
                                    OkHttpClient client = new OkHttpClient();

                                    RequestBody formBody = new FormBody.Builder()
                                            .add("text", code)
                                            .build();
                                    Request request = new Request.Builder()
                                            .url(Util.SET_CODE_VALID_URL)
                                            .post(formBody)
                                            .build();

                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            Util.Log("Can't set code as expired: " + e);
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response)
                                                throws IOException {
                                            if (!response.isSuccessful()) return;

                                            Util.Log("Make code for game " + game.getName() + " valid");
                                        }
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Can't parse game
                        Util.Log("Can't parse game home screen: " + e);
                    }
                }

                if (mFirebaseUser == null) return;
                try {
                    database.child("game_invites").child(mFirebaseUser.getUid()).addValueEventListener
                            (new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot gameInviteSnapshot : dataSnapshot.getChildren()) {
                                        try {
                                            GameInvite gameInvite = gameInviteSnapshot.getValue(GameInvite.class);
                                            if (gameInvite.getGame() != null) {
                                                GameInvite.Game game = gameInvite.getGame();
                                                if (mEventTimes.get(game.getEventId()) > 0) {
                                                    game.setEventTime(mEventTimes.get(game.getEventId()));
                                                    game.setInviteName(gameInvite.getName());
                                                    game.setInviteId(gameInviteSnapshot.getKey());

                                                    if (!mGames.contains(game)) mGames.add(game);
                                                }
                                            }
                                        } catch (Exception e) {
                                        }
                                    }

                                    try {
                                        Collections.sort(mGames, new GameComparator());
                                        mAdapter.notifyDataSetChanged();

                                        // Update the title text
                                        mTitleTxt.setText(mGames.size() > 0 ? R.string.games_title
                                                : R.string.games_title_empty);
                                    } catch (Exception e) {
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Collections.sort(mGames, new GameComparator());
                                    mAdapter.notifyDataSetChanged();

                                    // Update the title text
                                    mTitleTxt.setText(mGames.size() > 0 ? R.string.games_title
                                            : R.string.games_title_empty);
                                }
                            });
                } catch (Exception e) {
                    // User already logged out
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ArrayList<GameInvite.Game>> call, Throwable t) {
                Util.Log("Can't get games on home screen: " + t);
            }
        });
    }

    public class GameComparator implements Comparator<GameInvite.Game> {
        public int compare(GameInvite.Game game1, GameInvite.Game game2) {
            int c;
            c = game2.getInviteIdCompare().compareTo(game1.getInviteIdCompare());
            if (c == 0)
                c = game2.getEventTime().compareTo(game1.getEventTime());
            return c;
        }
    }

    private void initRecycler() {
        mGamesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGamesRecycler.setHasFixedSize(true);
        String myId = null;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            myId = user.getUid();
        }
        mAdapter = new GamesAdapter((MainActivity) getActivity(), this, mGames, myId);
        mGamesRecycler.setAdapter(mAdapter);
        registerForContextMenu(mGamesRecycler);
    }

    private void initButtons() {
        //mHostBtn.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        startActivity(new Intent(getActivity(), HostActivity.class));
        //    }
        //});
        //mJoinBtn.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        startActivity(new Intent(getActivity(), JoinActivity.class));
        //    }
        //});
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupWindow popupwindow_obj = popupDisplay();
            popupwindow_obj.showAtLocation(fab, Gravity.TOP, fab.getLeft(), fab.getTop() - 48 );
            }
        });
        //mHowToBtn.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
         //       startActivity(new Intent(getActivity(), HowToPlayActivity.class));
        //    }
        //});
        //mHowToUseBtn.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        startActivity(new Intent(getActivity(), HowToUseActivity.class));
        //    }Check game
        //});
    }

    public PopupWindow popupDisplay() {
        popupWindow = new PopupWindow(getActivity());

        // inflate your layout or dynamically add view
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view;

        view = inflater.inflate(R.layout.popup_host_join, null);

        view.findViewById(R.id.game_join).setOnClickListener(menuClickListener);
        view.findViewById(R.id.game_host).setOnClickListener(menuClickListener);

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);

        return popupWindow;
    }

    View.OnClickListener menuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            popupWindow.dismiss();

            if (v.getId() == R.id.game_host) {
                startActivity(new Intent(getActivity(), HostActivity.class));
            } else if (v.getId() == R.id.game_join) {
                startActivity(new Intent(getActivity(), JoinActivity.class));
            }
        }
    };

    public void joinGame(String gameId) {
        startActivity(new Intent(getActivity(), GameBoardActivity.class)
                .putExtra(GameBoardActivity.EXTRA_GAME_ID, gameId));
    }

    public void deleteGame(final GameInvite.Game game) {
        mGameToRemoveId = game.getId();

        retrofit2.Call<Event> call = ApiFactory.getApiService().getEvent(game.getEventId());
        call.enqueue(new retrofit2.Callback<Event>() {
            @Override
            public void onResponse(retrofit2.Call<Event> call,
                                   retrofit2.Response<Event> response) {
                if (!response.isSuccessful()) return;

                Event event = response.body();
                if (event != null) {

                    if (!game.getAuthor().getUserId().equals(mFirebaseUser.getUid())) {
                        mRemoveGame = game;
                        getActivity().openContextMenu(mGamesRecycler);
                    } else {
                        if (event.getTimeText().toLowerCase().contains("final")
                                || (game.getSelectedSquares() == null)) {
                            mRemoveGame = game;
                            getActivity().openContextMenu(mGamesRecycler);
                        } else {
                            if (getActivity() == null) return;
                            Toast.makeText(getActivity(),
                                    "You can only delete this game if it's finished or empty",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), "You can't remove this game", Toast.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Event> call, Throwable t) {
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (mRemoveGame.getAuthor().getUserId().equals(mFirebaseUser.getUid())) {
            menu.add("Delete game");
        } else {
            menu.add("Leave game");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!mRemoveGame.getAuthor().getUserId().equals(mFirebaseUser.getUid())) {
            ArrayList<Player> players = mRemoveGame.getPlayers();
            if (players == null) players = new ArrayList<>();

            ArrayList<Player> playersCopy = new ArrayList<>(players);

            try {
                for (Player player : players) {
                    if (player.getUserId().equals(FirebaseAuth.getInstance()
                            .getCurrentUser().getUid())) {
                        playersCopy.remove(player);
                    }
                }
            } catch (Exception e) {
                // Can't delete game
            }

            players = playersCopy;

            mRemoveGame.setPlayers(players);
            updateGameOnServer(mRemoveGame);
        } else {
            deleteGameOnServer(mGameToRemoveId);
        }
        return true;
    }

    private void deleteGameOnServer(String id) {
        retrofit2.Call<Void> call = ApiFactory.getApiService().deleteGame(id);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call,
                                   retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    loadGames();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
            }
        });
    }

    private void updateGameOnServer(GameInvite.Game game) {
        retrofit2.Call<Void> call = ApiFactory.getApiService().updateGame(game);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call,
                                   retrofit2.Response<Void> response) {
                loadGames();
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
            }
        });
    }
}
