package com.moyersoftware.contender.game;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.adapter.JoinGamesAdapter;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.network.ApiFactory;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class JoinFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    // Constants
    private static final int LOCATION_PERMISSION_CODE = 0;
    private static final float GAME_SEARCH_RADIUS_MILES = 5;
    private static final float GAME_SEARCH_RADIUS_METERS = (float) (GAME_SEARCH_RADIUS_MILES
            * 1609.34);

    // Views
    @BindView(R.id.join_id_recycler)
    RecyclerView mGamesIdRecycler;
    @BindView(R.id.join_id_edit_txt)
    EditText mIdEditTxt;
    @BindView(R.id.join_id_title_txt)
    TextView mTitleIdTxt;
    //@BindView(R.id.join_location_recycler)
    //RecyclerView mGamesLocationRecycler;
    //@BindView(R.id.join_location_search_txt)
    //TextView mSearchTxt;

    // Usual variables
    private final ArrayList<GameInvite.Game> mIdGames = new ArrayList<>();
    private JoinGamesAdapter mIdAdapter;
    private String mQuery;
    private DataSnapshot mIdDataSnapshot;
    private String mMyId;
    private String mMyEmail;
    private String mMyName;
    private String mMyPhoto;
    private final HashMap<String, Long> mIdEventTimes = new HashMap<>();
    private ArrayList<GameInvite.Game> mIdGamesSnapshot;

    private final ArrayList<GameInvite.Game> mLocationGames = new ArrayList<>();
    private JoinGamesAdapter mLocationAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Location mMyLocation;
    private final HashMap<String, Long> mLocationEventTimes = new HashMap<>();
    private ArrayList<GameInvite.Game> mLocationGamesSnapshot;
    private ArrayList<GameInvite.Game> mGamesSnapshot;

    public JoinFragment() {
        // Required empty public constructor
    }

    public static JoinFragment newInstance() {
        return new JoinFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join, container, false);
        ButterKnife.bind(this, view);

        initUser();
        initSearchField();
        initIdRecycler();
        //initLocationRecycler();
        initDatabase();
        initGoogleClient();

        return view;
    }

    private void initGoogleClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void initUser() {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mMyId = firebaseUser.getUid();
            mMyEmail = firebaseUser.getEmail();
            mMyName = Util.getDisplayName();
            mMyPhoto = Util.getPhoto();
        }
    }

    private void initSearchField() {
        mIdEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mQuery = mIdEditTxt.getText().toString();
                updateIdGames(mGamesSnapshot);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initIdRecycler() {
        mGamesIdRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGamesIdRecycler.setHasFixedSize(true);
        mIdAdapter = new JoinGamesAdapter((JoinActivity) getActivity(), mIdGames, mMyId, mMyEmail,
                mMyName, mMyPhoto);
        mGamesIdRecycler.setAdapter(mIdAdapter);
    }

    //private void initLocationRecycler() {
    ///    mGamesLocationRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
    //    mGamesLocationRecycler.setHasFixedSize(true);
    //    mLocationAdapter = new JoinGamesAdapter((JoinActivity) getActivity(), mLocationGames,
    //            mMyId, mMyEmail,
   //             mMyName, mMyPhoto);
    //    mGamesLocationRecycler.setAdapter(mLocationAdapter);
   // }

    private void initDatabase() {
        retrofit2.Call<ArrayList<GameInvite.Game>> call = ApiFactory.getApiService().getGames();
        call.enqueue(new retrofit2.Callback<ArrayList<GameInvite.Game>>() {
            @Override
            public void onResponse(retrofit2.Call<ArrayList<GameInvite.Game>> call,
                                   retrofit2.Response<ArrayList<GameInvite.Game>> response) {
                mGamesSnapshot = response.body();
                updateIdGames(mGamesSnapshot);
            }

            @Override
            public void onFailure(retrofit2.Call<ArrayList<GameInvite.Game>> call, Throwable t) {
            }
        });
    }

    /**
     * Updates the games list.
     * @param gamesSnapshot
     */
    private void updateIdGames(ArrayList<GameInvite.Game> gamesSnapshot) {
        if (gamesSnapshot == null) return;

        mIdGamesSnapshot = gamesSnapshot;

        retrofit2.Call<ArrayList<Event>> call = ApiFactory.getApiService().getEvents();
        call.enqueue(new retrofit2.Callback<ArrayList<Event>>() {
            @Override
            public void onResponse(retrofit2.Call<ArrayList<Event>> call,
                                   retrofit2.Response<ArrayList<Event>> response) {
                if (!response.isSuccessful()) return;

                mIdEventTimes.clear();
                for (Event event : response.body()) {
                    try {
                        if (event.getTime() > 0) {
                            mIdEventTimes.put(event.getId(), event.getTime());
                        } else {
                            mIdEventTimes.put(event.getId(), event.getTime());
                        }
                    } catch (Exception e) {
                        // Can't retrieve game time
                    }
                }
                mIdGames.clear();
                if (mMyId != null) {
                    for (GameInvite.Game game : mIdGamesSnapshot) {
                        try {
                            if (!mIdEventTimes.containsKey(game.getEventId())) continue;

                            game.setEventTime(mIdEventTimes.get(game.getEventId()));

                            if (!TextUtils.isEmpty(mQuery) && game.getId().contains(mQuery)
                                    && game.getAuthor() != null && !game.getAuthor().getUserId()
                                    .equals(mMyId) && game.getEventTime() != -1
                                    && game.getEventTime() != -2 && game.getEventTime()
                                    > System.currentTimeMillis()) {
                                mIdGames.add(game);
                            }
                        } catch (Exception e) {
                            Util.Log("The game is corrupted: " + e);
                        }
                    }
                }
                mIdAdapter.setGames(mIdGames);
                mIdAdapter.notifyDataSetChanged();

                mTitleIdTxt.setVisibility(mIdGames.size() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(retrofit2.Call<ArrayList<Event>> call, Throwable t) {
            }
        });
    }

    /**
     * Updates the games list.
     * @param dataSnapshot
     */
    private void updateLocationGames(ArrayList<GameInvite.Game> dataSnapshot) {
        if (dataSnapshot == null) return;

        mLocationGamesSnapshot = dataSnapshot;

        retrofit2.Call<ArrayList<Event>> call = ApiFactory.getApiService().getEvents();
        call.enqueue(new retrofit2.Callback<ArrayList<Event>>() {
            @Override
            public void onResponse(retrofit2.Call<ArrayList<Event>> call,
                                   retrofit2.Response<ArrayList<Event>> response) {
                if (!response.isSuccessful()) return;
                mLocationEventTimes.clear();
                for (Event event : response.body()) {
                    try {
                        mLocationEventTimes.put(event.getId(), event.getTime());
                    } catch (Exception e) {
                        // Can't retrieve game time
                    }
                }

                mLocationGames.clear();
                if (mMyId != null) {
                    for (GameInvite.Game game : mLocationGamesSnapshot) {
                        if (game.getLatitude() != 0 && game.getLongitude() != 0) {
                            Location gameLocation = new Location("");
                            gameLocation.setLatitude(game.getLatitude());
                            gameLocation.setLongitude(game.getLongitude());
                            if (mMyLocation != null && mMyLocation.distanceTo(gameLocation)
                                    < GAME_SEARCH_RADIUS_METERS && !game.getAuthor().getUserId()
                                    .equals(mMyId)) {
                                mLocationGames.add(game);
                            }
                        }
                    }
                }
                mLocationAdapter.notifyDataSetChanged();

                //if (mLocationGames.size() > 0) {
                //    mSearchTxt.setVisibility(View.GONE);
               // } else {
                //    mSearchTxt.setText(R.string.join_location_empty);
                //    mSearchTxt.setVisibility(View.VISIBLE);
               // }
            }

            @Override
            public void onFailure(retrofit2.Call<ArrayList<Event>> call, Throwable t) {
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_CODE);
            } else {
                mMyLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
        } else {
            mMyLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        updateLocationGames(mGamesSnapshot);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMyLocation = LocationServices.FusedLocationApi.getLastLocation
                            (mGoogleApiClient);
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
