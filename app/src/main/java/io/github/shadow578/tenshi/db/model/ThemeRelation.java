package io.github.shadow578.tenshi.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * describes a relation between a anime and a theme
 */
@Entity(tableName = "theme_relations",
        primaryKeys = {
                "anime_id",
                "theme_id"
        })
public class ThemeRelation {
    /**
     * id of the anime that has this genre
     * {@link io.github.shadow578.tenshi.mal.model.Anime#animeId}
     */
    @ColumnInfo(name = "anime_id")
    public int animeId;

    /**
     * id of the genre
     * {@link io.github.shadow578.tenshi.mal.model.Theme#id}
     */
    @ColumnInfo(name = "theme_id")
    public int themeId;

    /**
     * is this a ending theme?
     */
    @ColumnInfo(name = "ending_theme")
    public boolean isEndingTheme;
}