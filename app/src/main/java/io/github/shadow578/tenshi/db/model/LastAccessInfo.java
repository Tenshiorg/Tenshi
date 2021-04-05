package io.github.shadow578.tenshi.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * contains info about when a {@link io.github.shadow578.tenshi.mal.model.Anime} or {@link io.github.shadow578.tenshi.mal.model.User} entity was last accessed in the database
 * used to clear out unused entities
 */
@SuppressWarnings("CanBeFinal")
@Entity(tableName = "last_access",
        indices = {
                @Index(value = "id", unique = true)
        })
public class LastAccessInfo {
    /**
     * id of the target element
     * either {@link io.github.shadow578.tenshi.mal.model.Anime#animeId} or {@link io.github.shadow578.tenshi.mal.model.User#userID}
     */
    @ColumnInfo(name = "id")
    @PrimaryKey
    public int id;

    /**
     * is the {@link #id} one of a {@link io.github.shadow578.tenshi.mal.model.Anime} or a {@link io.github.shadow578.tenshi.mal.model.User}
     */
    @ColumnInfo(name = "target_is_anime")
    public boolean isAnime;

    /**
     * when this element was last accessed
     */
    @ColumnInfo(name = "last_access_at")
    public long lastAccessedTimestamp;

    public LastAccessInfo(int id, boolean isAnime, long lastAccessedTimestamp) {
        this.id = id;
        this.isAnime = isAnime;
        this.lastAccessedTimestamp = lastAccessedTimestamp;
    }
}
