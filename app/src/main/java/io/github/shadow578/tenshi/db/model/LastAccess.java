package io.github.shadow578.tenshi.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.ZonedDateTime;

/**
 * contains info about when a anime entity was last accessed in the database
 * used to clear out unused entities
 */
@Entity(tableName = "last_access",
        indices = {
                @Index(value = "anime_id", unique = true)
        })
public class LastAccess {
    /**
     * id of the anime that has been accessed
     * {@link io.github.shadow578.tenshi.mal.model.Anime#animeId}
     */
    @ColumnInfo(name = "anime_id")
    @PrimaryKey
    public int animeId;

    /**
     * when this anime was last accessed
     */
    @ColumnInfo(name = "last_access_at")
    public ZonedDateTime lastAccessedAt;


}
