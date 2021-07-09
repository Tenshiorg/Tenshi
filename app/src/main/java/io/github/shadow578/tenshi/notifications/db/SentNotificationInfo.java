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

        info.description = fmt("id: %d; title: %s; text: %s; channel: %s; extra: %s",
                notificationId, contentTitle, contentText, channelID,
                extraBuilder.toString());
        info.notificationIdentifier = longHash(info.description);

        // set expiration
        info.expirationTimestamp = DateHelper.toEpoch(DateHelper.getLocalTime().plus(timeToLive));
        return info;
    }

    /**
     * calculate the hash of a string, but using a 64bit hash instead of 32 bit
     *
     * @param string the string to hash
     * @return the hash of the string
     */
    private static long longHash(String string) {
        long h = 1125899906842597L;
        for (char c : string.toCharArray()) {
            h = 31 * h + c;
        }
        return h;
    }

    /**
     * the unique notification identifier. this is unique for each notification with equal content
     */
    @PrimaryKey
    @ColumnInfo(name = "identifier")
    public long notificationIdentifier;

    /**
     * cleartext notification identifier
     * TODO for testing only, remove in production
     */
    @NonNull
    public String description = "";

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

        final SentNotificationInfo that = (SentNotificationInfo) o;
        return notificationIdentifier == that.notificationIdentifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationIdentifier);
    }
}
