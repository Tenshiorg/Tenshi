package io.github.shadow578.tenshi.db.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.time.LocalDateTime;
import java.util.List;

import io.github.shadow578.tenshi.db.model.LastAccessInfo;
import io.github.shadow578.tenshi.util.DateHelper;

/**
 * DAO for accessing {@link LastAccessInfo} entries in the db
 */
@Dao
public abstract class LastAccessInfoDao {
    /**
     * get all access info for anime or users that where accessed before a given time
     *
     * @param time the time that all users should be last accessed before
     * @return the access infos
     */
    @Transaction
    public List<LastAccessInfo> getBefore(@NonNull LocalDateTime time) {
        return _getAccess(DateHelper.toEpoch(time));
    }

    /**
     * update the access entry for a anime
     *
     * @param animeId the anime that was accessed.
     */
    @Transaction
    public void updateForAnime(int animeId) {
        _insertOrUpdateAccess(new LastAccessInfo(animeId, true, now()));
    }

    /**
     * update the access entry for a user
     *
     * @param userId the user that was accessed
     */
    @Transaction
    public void updateForUser(int userId) {
        _insertOrUpdateAccess(new LastAccessInfo(userId, false, now()));
    }

    /**
     * @return the current epoch value
     */
    private long now() {
        return DateHelper.toEpoch(DateHelper.getLocalTime());
    }

    // region direct DAO

    /**
     * select all access infos before the given epoch
     *
     * @param epochBefore the epoch before
     * @return all access infos
     */
    @Query("SELECT * FROM last_access WHERE last_access_at < :epochBefore")
    protected abstract List<LastAccessInfo> _getAccess(long epochBefore);

    /**
     * insert or update a access information
     *
     * @param access the entry to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void _insertOrUpdateAccess(LastAccessInfo access);

    /**
     * delete a last access information
     *
     * @param id the target id of the entry to delete
     */
    @Query("DELETE FROM last_access WHERE id = :id")
    public abstract void deleteAccessFor(int id);
    //endregion
}
