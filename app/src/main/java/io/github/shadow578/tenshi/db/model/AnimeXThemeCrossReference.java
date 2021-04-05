package io.github.shadow578.tenshi.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * describes a relation between a anime and a theme
 */
@SuppressWarnings("CanBeFinal")
@Entity(tableName = "anime_theme_ref",
        primaryKeys = {
                "anime_id",
                "theme_id"
        })
public class AnimeXThemeCrossReference {
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
     * is this theme a ending theme of the anime?
     */
    @ColumnInfo(name = "is_ending")
    public boolean isEnding;

    public AnimeXThemeCrossReference(int animeId, int themeId, boolean isEnding) {
        this.animeId = animeId;
        this.themeId = themeId;
        this.isEnding = isEnding;
    }
}