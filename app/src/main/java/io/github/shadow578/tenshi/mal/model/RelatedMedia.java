package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import io.github.shadow578.tenshi.mal.model.type.RelationType;

/**
 * a {@link Anime} that is related to another Anime
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class RelatedMedia {
    /**
     * the anime that is related
     */
    @SerializedName("node")
    public Anime relatedAnime;

    /**
     * raw relation type
     */
    @Nullable
    @SerializedName("relation_type")
    public RelationType relationType;

    /**
     * relation type, formatted by MAL
     */
    @Nullable
    @SerializedName("relation_type_formatted")
    public String relationTypeFormatted;
}
