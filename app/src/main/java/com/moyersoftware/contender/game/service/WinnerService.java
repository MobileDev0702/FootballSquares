package com.moyersoftware.contender.game.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.GameBoardActivity;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.menu.data.PaidPlayer;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.network.ApiFactory;
import com.moyersoftware.contender.util.MyApplication;
import com.moyersoftware.contender.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

import static com.moyersoftware.contender.game.GameBoardActivity.EXTRA_GAME_ID;

public class WinnerService extends Service {

    private static final int PAID_NOTIFICATION_CODE = 123;
    private static final String CHANNEL_ID = "1";
    private final HashMap<String, Long> mEventTimes = new HashMap<>();
    private final ArrayList<Integer> mShownPaidNotifications = new ArrayList<>();
    private Handler mHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        addGamesListener();
        Util.Log("add games listener");
    }

    private void addGamesListener() {
        retrofit2.Call<ArrayList<Event>> call = ApiFactory.getApiService().getEvents();
        call.enqueue(new retrofit2.Callback<ArrayList<Event>>() {
            @Override
            public void onResponse(retrofit2.Call<ArrayList<Event>> call,
                                   retrofit2.Response<ArrayList<Event>> response) {
                if (!response.isSuccessful()) return;

                mEventTimes.clear();
                ArrayList<Event> events = response.body();
                for (Event event : events) {
                    try {
                        mEventTimes.put(event.getId(), event.getTime());
                    } catch (Exception e) {
                        // Can't retrieve game time
                    }
                }

                getGames(FirebaseAuth.getInstance().getCurrentUser());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getGames(FirebaseAuth.getInstance().getCurrentUser());
                        new Handler().postDelayed(this, 60 * 1000);
                    }
                }, 60 * 1000);
            }

            @Override
            public void onFailure(retrofit2.Call<ArrayList<Event>> call, Throwable t) {
            }
        });

        mHandler = new Handler();
        mStatusChecker.run();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mStatusChecker);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                getGames(FirebaseAuth.getInstance().getCurrentUser());
            } catch (Exception e) {
                Util.Log("Can't retrieve games: " + e);
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, 60 * 1000);
            }
        }
    };

    private void getGames(final FirebaseUser firebaseUser) {
        retrofit2.Call<ArrayList<GameInvite.Game>> call = ApiFactory.getApiService().getGames();
        call.enqueue(new retrofit2.Callback<ArrayList<GameInvite.Game>>() {
            @Override
            public void onResponse(retrofit2.Call<ArrayList<GameInvite.Game>> call,
                                   retrofit2.Response<ArrayList<GameInvite.Game>> response) {
                parseGames(firebaseUser, response.body());
            }

            @Override
            public void onFailure(retrofit2.Call<ArrayList<GameInvite.Game>> call, Throwable t) {
            }
        });
    }

    private void parseGames(FirebaseUser firebaseUser, ArrayList<GameInvite.Game> games) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String deviceOwnerId = user.getUid();
            String myId = Util.getCurrentPlayerId();
            if (myId == null) myId = deviceOwnerId;

            if (games == null) return;

            for (GameInvite.Game game : games) {
                try {
                    if (game != null && game.getAuthor() != null && (game.getAuthor().getUserId().equals(firebaseUser
                            .getUid()) || (game.getPlayers() != null && game.getPlayers()
                            .contains(new Player(firebaseUser.getUid(), null,
                                    firebaseUser.getEmail(),
                                    Util.getDisplayName(), Util.getPhoto()))))) {
                        if (!TextUtils.isEmpty(game.getInviteName())) {
                            continue;
                        }
                        int emptySquaresCount;
                        if (game.getSelectedSquares() != null) {
                            emptySquaresCount = 100 - game.getSelectedSquares().size();
                        } else {
                            emptySquaresCount = 100;
                        }

                        if (emptySquaresCount == 100 && (mEventTimes.get(game.getEventId())
                                == -2 || mEventTimes.get(game.getEventId())
                                < System.currentTimeMillis())) {
                            continue;
                        }

                        if (emptySquaresCount > 0 && mEventTimes.get(game.getEventId()) != -2
                                && mEventTimes.get(game.getEventId()) -
                                System.currentTimeMillis() < 1000 * 60 * 30) {
                            if (!PreferenceManager.getDefaultSharedPreferences
                                    (MyApplication.getContext()).getBoolean
                                    (game.getId() + "reminder", false)) {
                                PreferenceManager.getDefaultSharedPreferences
                                        (MyApplication.getContext()).edit().putBoolean
                                        (game.getId() + "reminder", true).apply();
                                showReminderNotification(MyApplication.getContext(),
                                        game.getId(), game.getName(),
                                        mEventTimes.get(game.getEventId()),
                                        emptySquaresCount);
                            }
                        }

                        String gameId = game.getId();

                        if (game.getQuarter1Winner() != null) {
                            if (!game.getQuarter1Winner().isConsumed() && game.getQuarter1Winner().getPlayer() != null
                                    && game.getQuarter1Winner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getQuarter1Price(), "1st", null, gameId);
                                game.getQuarter1Winner().setConsumed(true);
                                updateGameOnServer(game);
                            } else if (!game.getQuarter1Winner().isConsumed() && deviceOwnerId.equals(game.getQuarter1Winner()
                                    .getPlayer().getCreatedByUserId())) {
                                showWinDialog(game.getQuarter1Price(), "1st", game.getQuarter1Winner().getPlayer()
                                        .getName(), gameId);
                                game.getQuarter1Winner().setConsumed(true);
                                updateGameOnServer(game);
                            }
                        }
                        if (game.getQuarter2Winner() != null) {
                            if (!game.getQuarter2Winner().isConsumed() && game.getQuarter2Winner().getPlayer() != null
                                    && game.getQuarter2Winner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getQuarter2Price(), "2nd", null, gameId);
                                game.getQuarter2Winner().setConsumed(true);
                                updateGameOnServer(game);
                            } else if (!game.getQuarter2Winner().isConsumed() && deviceOwnerId.equals(game.getQuarter2Winner()
                                    .getPlayer().getCreatedByUserId())) {
                                showWinDialog(game.getQuarter2Price(), "2nd", game.getQuarter2Winner().getPlayer()
                                        .getName(), gameId);
                                game.getQuarter2Winner().setConsumed(true);
                                updateGameOnServer(game);
                            }
                        }

                        if (game.getQuarter3Winner() != null) {
                            if (!game.getQuarter3Winner().isConsumed() && game.getQuarter3Winner().getPlayer() != null
                                    && game.getQuarter3Winner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getQuarter3Price(), "3rd", null, gameId);
                                game.getQuarter3Winner().setConsumed(true);
                                updateGameOnServer(game);
                            } else if (!game.getQuarter3Winner().isConsumed() && deviceOwnerId.equals(game.getQuarter3Winner()
                                    .getPlayer().getCreatedByUserId())) {
                                showWinDialog(game.getQuarter3Price(), "3rd", game.getQuarter3Winner().getPlayer()
                                        .getName(), gameId);
                                game.getQuarter3Winner().setConsumed(true);
                                updateGameOnServer(game);
                            }
                        }

                        if (game.getFinalWinner() != null) {
                            if (!game.getFinalWinner().isConsumed() && game.getFinalWinner().getPlayer() != null
                                    && game.getFinalWinner().getPlayer().getUserId().equals(myId)) {
                                showWinDialog(game.getFinalPrice(), "final", null, gameId);
                                game.getFinalWinner().setConsumed(true);
                                updateGameOnServer(game);
                            } else if (!game.getFinalWinner().isConsumed() && deviceOwnerId.equals(game.getFinalWinner()
                                    .getPlayer().getCreatedByUserId())) {
                                showWinDialog(game.getFinalPrice(), "final", game.getFinalWinner().getPlayer()
                                        .getName(), gameId);
                                game.getFinalWinner().setConsumed(true);
                                updateGameOnServer(game);
                            }
                        }


                        // Warn if not all players paid
                        if (game.getAuthor() != null && game.getAuthor().getUserId().equals(myId) && mEventTimes.get
                                (game.getEventId()) != -1 && mEventTimes.get(game.getEventId())
                                != -2 && System.currentTimeMillis() < mEventTimes.get
                                (game.getEventId())) {
                            loadPlayers(game, gameId, game.getName(),
                                    mEventTimes.get(game.getEventId()), myId);
                        }
                    }
                } catch (Exception e) {
                    Util.Log("Can't show winners: " + e);
                }
            }
        }
    }

    private void updateGameOnServer(GameInvite.Game game) {
        retrofit2.Call<Void> call = ApiFactory.getApiService().updateGame(game);
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

    private void loadPlayers(GameInvite.Game game, final String gameId,
                             final String name, final long time, final String myId) {
        ArrayList<String> players = new ArrayList<>();
        players.clear();
        players.add(myId);
        if (game.getPlayers() != null) {
            for (Player player : game.getPlayers()) {
                try {
                    players.add(player.getUserId());
                } catch (Exception e) {
                    Util.Log("Can't parse player");
                }
            }
        }

        loadPaidPlayers(game, gameId, players, name, time);
    }

    private void loadPaidPlayers(GameInvite.Game game, final String gameId,
                                 final ArrayList<String> players, final String name,
                                 final long time) {
        ArrayList<String> paidPlayers = new ArrayList<>();
        if (game.getPaidPlayers() != null) {
            for (PaidPlayer paidPlayer : game.getPaidPlayers()) {
                if (paidPlayer.paid()) {
                    paidPlayers.add(paidPlayer.getUserId());
                }
            }
        }

        if (paidPlayers.size() != players.size()) {
            showPaidNotification(MyApplication.getContext(), gameId, name, time);
        }
    }

    private void showPaidNotification(Context context, String id, String name, long time) {
        createNotificationChannel();
        int minutesBefore;

        if (time - System.currentTimeMillis() < 5 * 60 * 1000) {
            if (!mShownPaidNotifications.contains(PAID_NOTIFICATION_CODE + Integer.parseInt(id)
                    + 5)) {
                minutesBefore = 5;
            } else {
                return;
            }
        } else if (time - System.currentTimeMillis() < 15 * 60 * 1000) {
            if (!mShownPaidNotifications.contains(PAID_NOTIFICATION_CODE + Integer.parseInt(id)
                    + 15)) {
                minutesBefore = 15;
            } else {
                return;
            }
        } else if (time - System.currentTimeMillis() < 30 * 60 * 1000) {
            if (!mShownPaidNotifications.contains(PAID_NOTIFICATION_CODE + Integer.parseInt(id)
                    + 30)) {
                minutesBefore = 30;
            } else {
                return;
            }
        } else {
            return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.warning)
                .setContentTitle(name + " game starts in less than " + minutesBefore + " minutes")
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        mBuilder.setContentText("Not everybody paid for their squares yet!");
        Intent resultIntent = new Intent(context, GameBoardActivity.class)
                .putExtra(EXTRA_GAME_ID, id);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        // Builds the notification and issues it.
        mNotifyMgr.notify(PAID_NOTIFICATION_CODE + Integer.parseInt(id), notification);

        mShownPaidNotifications.add(PAID_NOTIFICATION_CODE + Integer.parseInt(id) + minutesBefore);
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showReminderNotification(Context context, String id, String name, long time,
                                          int emptySquaresCount) {
        createNotificationChannel();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.warning)
                .setContentTitle(name + " game starts @ " + Util.formatTime(time))
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        if (emptySquaresCount == 100) {
            mBuilder.setContentText("You didn't fill a single square yet!");
        } else {
            mBuilder.setContentText("Don't forget to fill " + emptySquaresCount + " more square(s)!");
        }
        Intent resultIntent = new Intent(context, GameBoardActivity.class)
                .putExtra(EXTRA_GAME_ID, id);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        // Builds the notification and issues it.
        mNotifyMgr.notify(Integer.parseInt(id + 100), notification);
    }


    private void showWinDialog(final int price, final String quarter, final String name, final String gameId) {
        createNotificationChannel();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MyApplication.getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notif)
                .setContentTitle("Congratulations!")
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(MyApplication.getContext(), R.color.colorPrimary));
        if (TextUtils.isEmpty(name)) {
            mBuilder.setContentText("You won " + price + " points in the " + quarter
                    + " quarter");
        } else {
            mBuilder.setContentText(name + " won " + price + " points in the " + quarter
                    + " quarter");
        }
        Intent resultIntent = new Intent(MyApplication.getContext(), GameBoardActivity.class)
                .putExtra(GameBoardActivity.EXTRA_GAME_ID, gameId);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        MyApplication.getContext(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);// Sets an ID for the notification
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        // Builds the notification and issues it.
        mNotifyMgr.notify((gameId + quarter).hashCode(), notification);
    }
}
