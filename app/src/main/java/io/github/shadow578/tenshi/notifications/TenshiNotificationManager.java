package io.github.shadow578.tenshi.notifications;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.NotificationCompat;

import java.time.ZonedDateTime;
import java.util.Random;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.util.DateHelper;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.cast;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;

/**
 * manager to send and schedule notifications
 */
public class TenshiNotificationManager {

    /**
     * context from init
     */
    @NonNull
    private final Context ctx;

    /**
     * random
     */
    @NonNull
    private final Random rnd = new Random();

    public TenshiNotificationManager(@NonNull Context ctx) {
        this.ctx = ctx;
    }

    /**
     * create a new notification builder in the given channel, with the app icon set as {@link NotificationCompat.Builder#setSmallIcon(int)}
     *
     * @param channel the channel for the notification
     * @return the notification builder
     */
    public NotificationCompat.Builder notificationBuilder(@NonNull TenshiNotificationChannel channel) {
        return new NotificationCompat.Builder(ctx, channel.id())
                .setSmallIcon(R.mipmap.ic_launcher);
    }

    /**
     * get a random notification id
     *
     * @return random notification id
     */
    public int randomId() {
        return rnd.nextInt();
    }

    // region immediately

    /**
     * immediately send a notification
     *
     * @param notification the notification
     */
    public void sendNow(@NonNull Notification notification) {
        sendNow(randomId(), notification);
    }

    /**
     * immediately send a notification
     *
     * @param notificationId the notification id
     * @param notification   the notification
     */
    public void sendNow(int notificationId, @NonNull Notification notification) {
        ctx.sendBroadcast(NotificationPublisher.getIntent(ctx, notificationId, notification));
    }

    // endregion

    // region scheduled

    /**
     * send a notification after a delay
     * {@link #sendAt(int, Notification, long)}
     *
     * @param notification the notification
     * @param delay        the millisecond delay until publishing the notification
     */
    public void sendIn(@NonNull Notification notification, long delay) {
        sendIn(randomId(), notification, delay);
    }

    /**
     * send a notification after a delay
     * {@link #sendAt(int, Notification, long)}
     *
     * @param notificationId the notification id
     * @param notification   the notification
     * @param delay          the millisecond delay until publishing the notification
     */
    public void sendIn(int notificationId, @NonNull Notification notification, long delay) {
        sendAt(notificationId, notification, System.currentTimeMillis() + delay);
    }

    /**
     * send a notification at a specific time
     * {@link #sendAt(int, Notification, long)}
     *
     * @param notification the notification
     * @param time         the time to send the notification at.
     */
    public void sendAt(@NonNull Notification notification, @NonNull ZonedDateTime time) {
        sendAt(randomId(), notification, time);
    }

    /**
     * send a notification at a specific time
     * {@link #sendAt(int, Notification, long)}
     *
     * @param notificationId the notification id
     * @param notification   the notification
     * @param time           the time to send the notification at.
     */
    public void sendAt(int notificationId, @NonNull Notification notification, @NonNull ZonedDateTime time) {
        // calculate millis timestamp to send at, in the system timezone
        long timestampMillis = DateHelper.toEpoch(time);

        // send notification
        sendAt(notificationId, notification, timestampMillis);
    }

    /**
     * send a notification at a specific time, using AlarmManager.
     *
     * @param notification the notification
     * @param timestamp    the time to send the notification at. see {@link System#currentTimeMillis()}
     */
    public void sendAt(@NonNull Notification notification, long timestamp) {
        sendAt(randomId(), notification, timestamp);
    }

    /**
     * send a notification at a specific time, using AlarmManager.
     *
     * @param notificationId the notification id
     * @param notification   the notification
     * @param timestamp      the time to send the notification at. see {@link System#currentTimeMillis()}
     */
    public void sendAt(int notificationId, @NonNull Notification notification, long timestamp) {
        // ensure the time is not in the past
        // if the time is in the past, log a error and adjust the target to send right away.
        if (System.currentTimeMillis() >= timestamp) {
            Log.e("Tenshi", "NotificationHelper#sendAt() target time is in the past! sending asap");
            timestamp = System.currentTimeMillis() + 10000;
        }

        //TODO log sendat time
        final ZonedDateTime time = DateHelper.fromEpoc(timestamp / 1000);
        Log.i("TenshiNotify", "schedule notification for timestamp " + timestamp + "(ms); time is " + time.toString());

        // create target intent
        final PendingIntent intent = NotificationPublisher.getPendingIntent(ctx, notificationId, notification);

        // get alarm manager
        final AlarmManager alarmManager = cast(ctx.getSystemService(Context.ALARM_SERVICE));
        if (isNull(alarmManager)) {
            Log.e("Tenshi", "failed to get alarm manager!");
            return;
        }

        // set the alarm
        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, timestamp, intent);

        //TODO: alarm manager does not persist between reboots.
        // this means that we'd have to store, and re- schedule, any notifications that are still pending when the device boots.
        // in theory, this would be really simple, right?
        // just store the notification in a database with the time it should be sent, and register a BOOT_COMPLETED listener to reschedule them...
        // well, turns out that storing (or serializing) the notification object isn't really possible easily...
        // so, maybe this will be added someday, but for now it's fine
    }

    // endregion
}
