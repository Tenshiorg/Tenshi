package io.github.shadow578.tenshi.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * describes a relation between a anime and a genre
 */
@SuppressWarnings("CanBeFinal")
@Entity(tableName = "anime_genre_ref",
        primaryKeys = {
                "anime_id",
                "genre_id"
        })
public class AnimeXGenreCrossReference {

    /**
     * id of the anime that has this genre
     * {@link io.github.shadow578.tenshi.mal.model.Anime#animeId}
     */
    @ColumnInfo(name = "anime_id")
    public int animeId;

    /**
     * id of the genre
     * {@link io.github.shadow578.tenshi.mal.model.Genre#id}
     */
    @ColumnInfo(name = "genre_id")
    public int genreId;

    public AnimeXGenreCrossReference(int animeId, int genreId) {
        this.animeId = animeId;
        this.genreId = genreId;
    }
}
