package io.github.shadow578.tenshi.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * describes a relation between a anime and a studio
 */
@Entity(tableName = "anime_studio_ref",
        primaryKeys = {
                "anime_id",
                "studio_id"
        })
public class AnimeXStudioCrossReference {

    /**
     * id of the anime that has this genre
     * {@link io.github.shadow578.tenshi.mal.model.Anime#animeId}
     */
    @ColumnInfo(name = "anime_id")
    public int animeId;

    /**
     * id of the studio
     * {@link io.github.shadow578.tenshi.mal.model.Studio#id}
     */
    @ColumnInfo(name = "studio_id")
    public int studioId;

    public AnimeXStudioCrossReference(int animeId, int studioId) {
        this.animeId = animeId;
        this.studioId = studioId;
    }
}
