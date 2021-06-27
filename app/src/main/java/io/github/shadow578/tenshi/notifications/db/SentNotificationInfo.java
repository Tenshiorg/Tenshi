package io.github.shadow578.tenshi.notifications.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

import io.github.shadow578.tenshi.util.DateHelper;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;

/**
 * info class to hold information to identify a notification
 */
@Entity(tableName = "sent_notifications")
public class SentNotificationInfo {

    /**
     * create a notification info from notification details
     *
     * @param timeToLive     how long the info should live
     * @param notificationId the notification ID
     * @param contentTitle   the title of the notification
     * @param contentText    the main text of the notification
     * @param channelID      the channel id the notification is posted in
     * @param extra          additional notification parameter. may be empty
     * @return the notification info
     */
    @NonNull
    public static SentNotificationInfo create(@NonNull Duration timeToLive,
                                              int notificationId,
                                              @NonNull String contentTitle,
                                              @NonNull String contentText,
                                              @NonNull String channelID,
                                              @NonNull String... extra) {
        // create identifier
        final SentNotificationInfo info = new SentNotificationInfo();
        final StringJoiner extraBuilder = new StringJoiner("; ");
        for (String e : extra)
            extraBuilder.add(e);

        info.notificationIdentifier = fmt("id: %d; title: %s; text: %s; channel: %s; extra: %s",
                notificationId, contentTitle, contentText, channelID,
                extraBuilder.toString());

        // TODO: use hash instead of clear- text identifier

        // set expiration
        info.expirationTimestamp = DateHelper.toEpoch(DateHelper.getLocalTime()) + timeToLive.getSeconds();
        return info;
    }

    /**
     * the unique notification identifier. this is unique for each notification with equal content
     */
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "identifier")
    public String notificationIdentifier = "";

    /**
     * when this notification expires
     */
    @ColumnInfo(name = "expiration")
    public long expirationTimestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        SentNotificationInfo that = (SentNotificationInfo) o;
        return notificationIdentifier.equals(that.notificationIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationIdentifier);
    }
}
