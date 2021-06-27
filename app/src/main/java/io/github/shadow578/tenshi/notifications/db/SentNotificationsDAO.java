package io.github.shadow578.tenshi.notifications.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;
import java.util.stream.Collectors;

import io.github.shadow578.tenshi.util.DateHelper;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;

/**
 * DAO for accessing sent notification info
 */
@Dao
public abstract class SentNotificationsDAO {

    /**
     * remove all notification info that are expired
     *
     * @return the number of removed entries
     */
    @Transaction
    public int removeExpired() {
        // get epoch from time
        final long epochNow = DateHelper.toEpoch(DateHelper.getLocalTime());

        // find all that expired
        final List<SentNotificationInfo> expired = _getExpired(epochNow);

        // cancel if none are expired
        if (nullOrEmpty(expired))
            return 0;

        // map to a list of identifiers
        final List<String> expiredIdentifiers = expired.stream()
                .map((info) -> info.notificationIdentifier)
                .collect(Collectors.toList());

        // remove all expired
        _deleteAll(expiredIdentifiers);
        return expired.size();
    }

    /**
     * insert a info into the database if it was not present
     *
     * @param info the info to insert
     * @return was the info inserted? if it was already present, the this will be false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Transaction
    public boolean insertIfNotPresent(SentNotificationInfo info) {
        // find info with that id, abort if found
        if (notNull(getInfo(info.notificationIdentifier)))
            return false;

        // not found, insert
        _insert(info);
        return true;
    }

    /**
     * get a notification info with the given identifier
     *
     * @param notificationIdentifier the identifier to find
     * @return the notification info, or null if not found
     */
    @Query("SELECT * FROM sent_notifications WHERE identifier = :notificationIdentifier")
    public abstract SentNotificationInfo getInfo(String notificationIdentifier);

    /**
     * insert a notification info into the db
     *
     * @param info the notification info to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void _insert(SentNotificationInfo info);

    /**
     * get all notification info that should be expired
     *
     * @param epochNow the current epoch time
     * @return a list of all expired notification info
     */
    @Query("SELECT * FROM sent_notifications WHERE expiration < :epochNow")
    protected abstract List<SentNotificationInfo> _getExpired(long epochNow);

    /**
     * delete all notification info with the given identifiers
     *
     * @param identifiersToDelete the list of identifiers to delete
     */
    @Query("DELETE FROM sent_notifications WHERE identifier IN (:identifiersToDelete)")
    protected abstract void _deleteAll(List<String> identifiersToDelete);
}
