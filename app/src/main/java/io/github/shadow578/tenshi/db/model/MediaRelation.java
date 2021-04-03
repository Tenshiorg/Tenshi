package io.github.shadow578.tenshi.db.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import io.github.shadow578.tenshi.mal.model.RelatedMedia;
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
    @ColumnInfo(name = "relation_type")
    public RelationType relationType;

    /**
     * relation type, formatted by MAL
     */
    @Nullable
    @ColumnInfo(name = "relation_type_formatted")
    public String relationTypeFormatted;

    /**
     * is the child a manga or anime?
     */
    @ColumnInfo(name = "is_manga")
    public boolean isManga;

    public MediaRelation(int parentId, int childId, @Nullable RelationType relationType, @Nullable String relationTypeFormatted, boolean isManga) {
        this.parentId = parentId;
        this.childId = childId;
        this.relationType = relationType;
        this.relationTypeFormatted = relationTypeFormatted;
        this.isManga = isManga;
    }

    /**
     * create a media relation from a related media
     *
     * @param parentId the parent id the media is related to
     * @param related  the related media (anime/manga)
     * @param isManga  is this relation for a manga (the child)
     * @return the relation
     */
    public static MediaRelation fromRelatedMedia(int parentId, @NonNull RelatedMedia related, boolean isManga) {
        return new MediaRelation(parentId, related.relatedAnime.animeId, related.relationType, related.relationTypeFormatted, isManga);
    }
}
