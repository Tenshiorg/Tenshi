package io.github.shadow578.tenshi.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * publishes a notification in a broadcast receiver
 */
public class NotificationPublisher extends BroadcastReceiver {

    /**
     * intent action for publishing a new notification
     */
    private static final String ACTION_PUBLISH_NOTIFICATION = "io.github.shadow578.tenshi.notifications.PUBLISH";

    /**
     * the notification id of the notification to publish
     */
    private static final String EXTRA_NOTIFICATION_ID = "notificationId";

    /**
     * the actual notification to publish
     */
    private static final String EXTRA_NOTIFICATION_CONTENT = "notificationData";

    /**
     * create a intent for publishing a notification
     *
     * @param ctx            the context to work in
     * @param notificationId the notification id to use
     * @param notification   the notification
     * @return the intent for the broadcast
     */
    @NonNull
    public static Intent getIntent(@NonNull Context ctx, int notificationId, @NonNull Notification notification) {
        final Intent nfIntent = new Intent(ctx, NotificationPublisher.class);
        nfIntent.setAction(ACTION_PUBLISH_NOTIFICATION);
        nfIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        nfIntent.putExtra(EXTRA_NOTIFICATION_CONTENT, notification);
        return nfIntent;
    }

    /**
     * create a pending intent for publishing a notification.
     * if a intent for the same notification id was already created, it is updated.
     *
     * @param ctx            the context to work in
     * @param notificationId the notification id to use
     * @param notification   the notification
     * @return the intent for the broadcast
     */
    @NonNull
    public static PendingIntent getPendingIntent(@NonNull Context ctx, int notificationId, @NonNull Notification notification) {
        final Intent nfIntent = getIntent(ctx, notificationId, notification);
        return PendingIntent.getBroadcast(ctx, notificationId, nfIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        // ensure context and intent are not null
        // and the intent has the right action
        if (notNull(ctx)
                && notNull(intent)
                && ACTION_PUBLISH_NOTIFICATION.equals(intent.getAction())) {
            // looking good, get values from extra
            final Notification notification = intent.getParcelableExtra(EXTRA_NOTIFICATION_CONTENT);
            final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);

            // make sure we have a notification to publish
            // default id is fine
            if (isNull(notification)) {
                Log.e("Tenshi", "ScheduledNotificationsPublisher received empty notification (id " + notificationId + ")");
                return;
            }

            // publish notification
            NotificationManagerCompat.from(ctx)
                    .notify(notificationId, notification);
        }
    }
}
