package io.github.shadow578.tenshi.mal.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * the genre of a {@link Anime}
 */
@Entity(tableName = "genres",
        indices = {
        @Index(value = "genre_id", unique = true)
})
public final class Genre {
    /**
     * MAL id of the genre
     */
    @ColumnInfo(name = "genre_id")
    @PrimaryKey
    public int id;

    /**
     * the name of the genre
     */
    @ColumnInfo(name = "name")
    public String name;
}
