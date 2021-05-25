package com.moyersoftware.contender.game.service.firebase;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.moyersoftware.contender.util.Util;

import org.json.JSONObject;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TYPE_GAMES_UPDATED = "games_updated";
    public static final String TYPE_EVENTS_UPDATED = "events_updated";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Util.Log("Received FCM message");

        try {
            Util.Log("FCM message: "+new Gson().toJson(new JSONObject(remoteMessage.getData())));
            JSONObject message = new JSONObject(remoteMessage.getData());
            if (message.getString("type").equals(TYPE_GAMES_UPDATED)) {
                Util.Log("Update games");
                sendBroadcast(new Intent(TYPE_GAMES_UPDATED));
            } else if (message.getString("type").equals(TYPE_EVENTS_UPDATED)) {
                Util.Log("Update events");
                sendBroadcast(new Intent(TYPE_EVENTS_UPDATED));
            }
        } catch (Exception e) {
            Util.Log("Can't parse FCM message: " + e);
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}
