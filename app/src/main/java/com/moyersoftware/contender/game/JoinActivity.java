package com.moyersoftware.contender.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.adapter.JoinPagerAdapter;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.network.ApiFactory;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class JoinActivity extends AppCompatActivity {

    // Views
    @BindView(R.id.join_pager)
    ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        ButterKnife.bind(this);

        initPager();
        initDatabase();
    }

    private void initPager() {
        mPager.setAdapter(new JoinPagerAdapter(getSupportFragmentManager()));
    }

    private void initDatabase() {
    }

    /**
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    public void onBackButtonClicked(View view) {
        finish();
    }

    public void joinGame(final String gameId, final String password) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_password, null);
        final EditText editText = view.findViewById(R.id.join_password_edit_txt);
        dialogBuilder.setView(view);
        dialogBuilder.setTitle("Enter game password");
        dialogBuilder.setPositiveButton("OK", null);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                // Open keyboard
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_FORCED,
                                InputMethodManager.HIDE_IMPLICIT_ONLY);

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (editText.getText().toString().equals(password)) {
                            // Hide keyboard
                            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                    .hideSoftInputFromWindow(editText.getWindowToken(), 0);
                            dialog.dismiss();
                            playGame(gameId);
                        } else {
                            Toast.makeText(JoinActivity.this, "Incorrect password",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        alertDialog.show();
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

                    if (!players.contains(new Player(firebaseUser.getUid(), null,
                            firebaseUser.getEmail(), Util.getDisplayName(),
                            Util.getPhoto()))) {
                        players.add(new Player(firebaseUser.getUid(), null,
                                firebaseUser.getEmail(), Util.getDisplayName(),
                                Util.getPhoto()));
                    }

                    game.setPlayers(players);

                    updateGameOnServer(game);
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
                startActivity(new Intent(JoinActivity.this, GameBoardActivity.class)
                        .putExtra(GameBoardActivity.EXTRA_GAME_ID, game.getId()));
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
            }
        });
    }
}
