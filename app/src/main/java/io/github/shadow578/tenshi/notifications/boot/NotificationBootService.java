package io.github.shadow578.tenshi.notifications.boot;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.notifications.db.SentNotificationsDB;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.async;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * service started by {@link NotificationBootListener} to clear scheduled notifications
 * from the notifications database on reboot
 */
public class NotificationBootService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // cannot bind this service
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Tenshi", "NotificationBootService started");

        // get or create DB instance if we don't have one
        final SentNotificationsDB db;
        if (notNull(TenshiApp.INSTANCE))
            db = TenshiApp.getNotifyDB();
        else
            db = SentNotificationsDB.create(getApplicationContext());

        // clear all scheduled so they can be re- sent
        async(() -> db.notificationsDB().removeScheduled(), (removed) -> {
            Log.i("Tenshi", fmt("removed %d scheduled notification entries in ON_BOOT", removed));

            // stop this service
            stopSelf();
        });

        return super.onStartCommand(intent, flags, startId);
    }
}
