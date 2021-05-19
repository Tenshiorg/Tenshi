package io.github.shadow578.tenshi.notifications;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.NotificationCompat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import io.github.shadow578.tenshi.R;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.cast;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;

/**
 * helper class to send and schedule notifications
 */
public class NotificationHelper {

    /**
     * create a new notification builder in the given channel, with the app icon set as {@link NotificationCompat.Builder#setSmallIcon(int)}
     * @param ctx the context to work in
     * @param channel the channel for the notification
     * @return the notification builder
     */
    public static NotificationCompat.Builder builder(@NonNull Context ctx, @NonNull TenshiNotificationChannel channel) {
        return new NotificationCompat.Builder(ctx, channel.id())
                .setSmallIcon(R.mipmap.ic_launcher);
    }

    /**
     * immediately send a notification
     *
     * @param ctx            the context to work in
     * @param notificationId the notification id
     * @param notification   the notification
     */
    public static void sendNow(@NonNull Context ctx, int notificationId, @NonNull Notification notification) {
        final PendingIntent intent = NotificationPublisher.getIntent(ctx, notificationId, notification);
        try {
            intent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * send a notification after a delay
     * {@link #sendAt(Context, int, Notification, long)}
     *
     * @param ctx            the context to work in
     * @param notificationId the notification id
     * @param notification   the notification
     * @param delay          the millisecond delay until publishing the notification
     */
    public static void sendIn(@NonNull Context ctx, int notificationId, @NonNull Notification notification, long delay) {
        sendAt(ctx, notificationId, notification, System.currentTimeMillis() + delay);
    }

    /**
     * send a notification at a specific time
     * {@link #sendAt(Context, int, Notification, long)}
     *
     * @param ctx            the context to work in
     * @param notificationId the notification id
     * @param notification   the notification
     * @param time           the time to send the notification at.
     */
    public static void sendAt(@NonNull Context ctx, int notificationId, @NonNull Notification notification, @NonNull LocalDateTime time) {
        // calculate millis timestamp to send at
        final ZonedDateTime zoned = time.atZone(ZoneId.systemDefault());
        long timestampMillis = zoned.toInstant().toEpochMilli();

        // send notification
        sendAt(ctx, notificationId, notification, timestampMillis);
    }

    /**
     * send a notification at a specific time, using AlarmManager.
     *
     * @param ctx            the context to work in
     * @param notificationId the notification id
     * @param notification   the notification
     * @param timestamp      the time to send the notification at. see {@link System#currentTimeMillis()}
     */
    public static void sendAt(@NonNull Context ctx, int notificationId, @NonNull Notification notification, long timestamp) {
        // ensure the time is not in the past
        // if the time is in the past, log a error and adjust the target to send right away.
        if (System.currentTimeMillis() >= timestamp) {
            Log.e("Tenshi", "NotificationHelper#sendAt() target time is in the past! sending asap");
            timestamp = System.currentTimeMillis() + 10000;
        }

        // create target intent
        final PendingIntent intent = NotificationPublisher.getIntent(ctx, notificationId, notification);

        // get alarm manager and set the alarm
        final AlarmManager alarmManager = cast(ctx.getSystemService(Context.ALARM_SERVICE));
        if (isNull(alarmManager)) {
            Log.e("Tenshi", "failed to get alarm manager!");
            return;
        }

        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, timestamp, intent);

        //TODO alarm manager does not persist between reboot, so we have to subscribe to onboot broadcast to re- schedule any pending notifications
        // ... at least in the future. right now, we should be fine :P
    }
}
