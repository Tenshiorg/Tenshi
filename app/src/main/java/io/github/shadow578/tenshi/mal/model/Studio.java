package io.github.shadow578.tenshi.mal.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * a studio that produced a {@link Anime}
 */
@Entity(tableName = "studios",
        indices = {
        @Index(value = "id", unique = true)
})
public final class Studio {
    /**
     * MAL id of this studio
     */
    @ColumnInfo(name = "id")
    @PrimaryKey
    public int id;

    /**
     * name of this studio
     */
    @ColumnInfo(name = "name")
    public String name;
}
