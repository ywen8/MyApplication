package com.example.dell.myapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.dell.myapplication.service.CallService;

public class AlarmreRecerve  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("arui.alarm.action")) {
            Intent i = new Intent();
            i.setClass(context, CallService.class);
            context.startService(i);
        }
    }
}
