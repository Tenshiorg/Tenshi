package io.github.shadow578.tenshi.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * describes a relation between a anime and a studio
 */
@Entity(tableName = "studio_relations",
        primaryKeys = {
                "anime_id",
                "studio_id"
        })
public class StudioRelation {

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
}
