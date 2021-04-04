package io.github.shadow578.tenshi.db.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * describes a anime / manga that was recommended for another anime / mange
 * <p>
 * db representation of {@link io.github.shadow578.tenshi.mal.model.RecommendedMedia}
 */
@Entity(tableName = "media_recommendations",
        primaryKeys = {
                "parent_id",
                "child_id"
        })
public class RecommendedMediaRelation {

    /**
     * {@link io.github.shadow578.tenshi.mal.model.Anime#animeId} of the parent anime the child was recommended for
     */
    @ColumnInfo(name = "parent_id")
    public int parentId;

    /**
     * {@link io.github.shadow578.tenshi.mal.model.Anime#animeId} of the child recommended for the parent
     */
    @ColumnInfo(name = "child_id")
    public int childId;

    /**
     * how often this anime was recommended (?)
     */
    @Nullable
    @ColumnInfo(name = "num_recommendations")
    public Integer recommendationCount;

    public RecommendedMediaRelation(int parentId, int childId, @Nullable Integer recommendationCount) {
        this.parentId = parentId;
        this.childId = childId;
        this.recommendationCount = recommendationCount;
    }
}
