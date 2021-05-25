package com.moyersoftware.contender.game.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.moyersoftware.contender.game.service.WinnerService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            context.startService(new Intent(context, WinnerService.class));
        } catch (Exception e) {

        }
    }
}
