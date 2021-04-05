package io.github.shadow578.tenshi.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import io.github.shadow578.tenshi.db.model.AnimeContentAdapterInfo;

/**
 * dao for content adapter info
 */
@Dao
public abstract class AnimeContentAdapterDao {

    /**
     * set the selected adapter for a anime.
     * the current selected adapter is overwritten
     *
     * @param animeId    the anime id
     * @param uniqueName the unique name of the selected adapter
     */
    @Transaction
    public void setSelectionFor(int animeId, String uniqueName) {
        // create record if needed
        _insert(new AnimeContentAdapterInfo(animeId, uniqueName, "", false));

        // set selection
        _setSelectionFor(animeId, uniqueName);
    }

    /**
     * set the persistent storage value for a anime and adapter
     *
     * @param animeId    the anime id
     * @param uniqueName the adapter unique name
     * @param storage    the storage value to set
     */
    @Transaction
    public void setPersistentStorage(int animeId, String uniqueName, String storage) {
        // create record if needed
        _insert(new AnimeContentAdapterInfo(animeId, uniqueName, "", false));

        // set persistent storage
        _setPersistentStorage(animeId, uniqueName, storage);
    }

    /**
     * get the selected adapter unique name for a anime
     *
     * @param animeId the anime id
     * @return the unique name of the selected adapter
     */
    @Query("SELECT unique_name FROM anime_content_adapters WHERE anime_id = :animeId AND is_selected = 1")
    public abstract String getSelectionFor(int animeId);

    /**
     * get the persistent storage value for a anime and adapter
     *
     * @param animeId    the anime id
     * @param uniqueName the adapter unique name
     * @return the persistent storage value
     */
    @Query("SELECT persistent_storage FROM anime_content_adapters WHERE anime_id = :animeId AND unique_name = :uniqueName")
    public abstract String getPersistentStorage(int animeId, String uniqueName);

    /**
     * insert a content adapter info into the db if it does not already exist
     *
     * @param info the info to insert
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void _insert(AnimeContentAdapterInfo info);

    /**
     * set the selected adapter for a anime.
     * the current selected adapter is overwritten
     *
     * @param animeId    the anime id
     * @param uniqueName the unique name of the selected adapter
     */
    @Query("UPDATE anime_content_adapters SET is_selected = (CASE WHEN unique_name = :uniqueName THEN 1 ELSE 0 END) WHERE anime_id = :animeId")
    protected abstract void _setSelectionFor(int animeId, String uniqueName);

    /**
     * set the persistent storage value for a anime and adapter
     *
     * @param animeId    the anime id
     * @param uniqueName the adapter unique name
     * @param storage    the storage value to set
     */
    @Query("UPDATE anime_content_adapters SET persistent_storage = :storage WHERE anime_id = :animeId AND unique_name = :uniqueName")
    protected abstract void _setPersistentStorage(int animeId, String uniqueName, String storage);

}
