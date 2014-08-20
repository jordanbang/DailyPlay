package com.jb.dailyplay.alarmreceiver;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.jb.dailyplay.R;
import com.jb.dailyplay.activities.MainActivity;
import com.jb.dailyplay.exceptions.NoSpaceException;
import com.jb.dailyplay.exceptions.NoWifiException;
import com.jb.dailyplay.managers.DailyMusicManager;
import com.jb.dailyplay.utils.DailyPlaySharedPrefUtils;
import com.jb.jblibs.LogUtils;
import com.noveogroup.android.log.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jordan on 6/30/2014.
 */
public class DailyPlayScheduledService extends IntentService{
    public static final String DAILY_PLAY_SCHEDULED_SERVICE = "DailyPlayScheduledService";
    public static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;

    public DailyPlayScheduledService() {
        super(DAILY_PLAY_SCHEDULED_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DailyPlaySharedPrefUtils.init(getApplication());
        DailyMusicManager dailyMusicManager = DailyMusicManager.getInstance();
        dailyMusicManager.login("george.doe231@gmail.com", "***REMOVED***");
        try {
            dailyMusicManager.getDailyPlayMusic(this);
            sendNotification("Songs successfully downloaded.  Enjoy your new DailyPlay list!");
        } catch(NoWifiException e) {
            Log.e(e);
            sendNotification("Your device was not connected to Wifi. We were unable to download a new DailyPlay list.");
        } catch(NoSpaceException e) {
            Log.e(e);
            sendNotification("Not enough space to download a new DailyPlay list");
        } catch (Exception e) {
            Log.e(e);
            LogUtils.appendLog(e);
            sendNotification("An error occurred while trying to download your DailyPlay list.");
        }

        Date date = new Date(System.currentTimeMillis());
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        Log.i("Daily Play List downloaded at " + format.format(date));
        DailyPlayAlarmReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String message) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle("DailyPlay list downloaded!").setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setContentText(message);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
