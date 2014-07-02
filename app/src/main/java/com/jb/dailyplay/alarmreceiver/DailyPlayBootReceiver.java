package com.jb.dailyplay.alarmreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Jordan on 7/1/2014.
 */
public class DailyPlayBootReceiver extends BroadcastReceiver {
    private DailyPlayAlarmReceiver mAlarm = new DailyPlayAlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            mAlarm.setAlarm(context);
        }
    }
}
