package com.jb.dailyplay.alarmreceiver;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.jb.dailyplay.R;
import com.jb.dailyplay.activities.MainActivity;
import com.jb.dailyplay.managers.DailyMusicManager;

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
        DailyMusicManager dailyMusicManager = new DailyMusicManager();
        dailyMusicManager.login("george.doe231@gmail.com", "GgfoDPxNSVH0Aqwx8MIt");
        dailyMusicManager.getDailyPlayMusic(5, this, null);

        sendNotification();
        DailyPlayAlarmReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification() {
        String message = "Songs successfully downloaded.  Enjoy your new DailyPlay list!";
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle("DailyPlay list downloaded!").setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setContentText(message);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
