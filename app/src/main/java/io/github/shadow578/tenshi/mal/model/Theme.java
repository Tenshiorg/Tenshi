package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * a Theme of a {@link Anime}.
 * Theme as in OP / ED. You know, music and stuff
 */
@Entity(tableName = "themes",
        indices = {
        @Index(value = "theme_id", unique = true)
})
public final class Theme {
    /**
     * the id of this theme
     */
    @ColumnInfo(name = "theme_id")
    @PrimaryKey
    public int id;

    /**
     * the text of this theme, commonly this is the (song) title
     */
    @Nullable
    @ColumnInfo(name = "text")
    public  String text;
}
