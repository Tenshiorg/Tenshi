package io.github.shadow578.tenshi.notifications.db;

import android.app.Notification;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * a notification that is scheduled
 */
@Entity(tableName = "scheduled_notifications",
        indices = {
                @Index(value = "notification_id", unique = true)
        })
public class ScheduledNotificationInfo {

    /**
     * the unique id of this notification
     */
    @ColumnInfo(name = "notification_id")
    @PrimaryKey
    public int notificationId;

    /**
     * the notification to send
     */
    @ColumnInfo(name = "notification_date")
    @Nullable
    public Notification notification;

    /**
     * millis timestamp when this notification should be sent. see {@link System#currentTimeMillis()}
     */
    @ColumnInfo(name = "send_at")
    public long targetTimestamp;

    public ScheduledNotificationInfo(int notificationId, @Nullable Notification notification, long targetTimestamp) {
        this.notificationId = notificationId;
        this.notification = notification;
        this.targetTimestamp = targetTimestamp;
    }
}
