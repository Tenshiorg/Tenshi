package io.github.shadow578.tenshi.notifications.db;

import android.app.Notification;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/**
 * DAO for (pending) scheduled notifications
 */
@Dao
public abstract class NotificationsDAO {

    /**
     * add a notification to the database
     *
     * @param notificationId the notification id
     * @param notification   the notification content
     * @param timestamp      the time at which the notification should be sent, in millis. see {@link System#currentTimeMillis()}
     */
    @Transaction
    public void addNotification(int notificationId, Notification notification, long timestamp) {
        // create info
        final ScheduledNotificationInfo info = new ScheduledNotificationInfo(notificationId, notification, timestamp);

        // insert into db
        _insertNotification(info);
    }

    /**
     * get all pending scheduled notifications
     *
     * @return all pending scheduled notifications
     */
    @Query("SELECT * FROM scheduled_notifications")
    public abstract List<ScheduledNotificationInfo> getAllPending();

    /**
     * remove a scheduled notification from the database
     *
     * @param notificationId the notification id to remove
     */
    @Query("DELETE FROM scheduled_notifications WHERE notification_id = :notificationId")
    public abstract void removeNotification(int notificationId);

    /**
     * insert a scheduled notification in the database
     *
     * @param info the notification to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void _insertNotification(ScheduledNotificationInfo info);
}
