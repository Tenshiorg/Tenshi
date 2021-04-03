package io.github.shadow578.tenshi.db.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.google.gson.annotations.SerializedName;

import io.github.shadow578.tenshi.mal.model.type.RelationType;

/**
 * describes a relation between two anime / manga
 * <p>
 * db representation of {@link io.github.shadow578.tenshi.mal.model.RelatedMedia}
 */
@Entity(tableName = "relations",
        primaryKeys = {
                "parent_id",
                "child_id"
        })
public class MediaRelation {

    /**
     * {@link io.github.shadow578.tenshi.mal.model.Anime#animeId} of the parent anime the child is related to
     */
    @ColumnInfo(name = "parent_id")
    public int parentId;

    /**
     * {@link io.github.shadow578.tenshi.mal.model.Anime#animeId} of the child related to the parent
     */
    @ColumnInfo(name = "child_id")
    public int childId;

    /**
     * raw relation type
     */
    @Nullable
    @SerializedName("relation_type")
    @ColumnInfo(name = "relation_type")
    public RelationType relationType;

    /**
     * relation type, formatted by MAL
     */
    @Nullable
    @SerializedName("relation_type_formatted")
    @ColumnInfo(name = "relation_type_formatted")
    public String relationTypeFormatted;
}
