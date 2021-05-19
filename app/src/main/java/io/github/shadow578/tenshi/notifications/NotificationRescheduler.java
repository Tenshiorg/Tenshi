package io.github.shadow578.tenshi.notifications;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.notifications.db.ScheduledNotificationInfo;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * a broadcast receiver that listens for {@link Intent#ACTION_BOOT_COMPLETED} and reschedules all
 * pending notifications with {@link AlarmManager}
 */
public class NotificationRescheduler extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        // check this is a valid broadcast
        // if ok, reschedule all pending notifications
        if (notNull(intent)
                && (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction()))) {
            // get notifications manager instance
            // create the instance if the app is not running
            final TenshiNotificationManager notifyMgr;
            if (notNull(TenshiApp.INSTANCE)) {
                // we have a app instance, assume the manager is ok too
                notifyMgr = TenshiApp.getNotifyManager();
            } else {
                // no app instance available, have to init manager ourselves
                notifyMgr = new TenshiNotificationManager(ctx);
            }

            // get all pending notifications
            final List<ScheduledNotificationInfo> pendingNotifications = notifyMgr.getNotificationsDB().notificationsDB().getAllPending();

            // re- schedule all
            for (ScheduledNotificationInfo n : pendingNotifications)
                if (notNull(n.notification))
                    notifyMgr.sendAtImpl(n.notificationId, n.notification, n.targetTimestamp, false);
        }
    }
}
