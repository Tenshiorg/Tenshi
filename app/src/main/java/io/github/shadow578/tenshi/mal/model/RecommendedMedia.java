package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * a {@link Anime} recommended
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class RecommendedMedia {
    /**
     * the recommended anime
     */
    @SerializedName("node")
    @NonNull
    public Anime animeRecommendation = new Anime();

    /**
     * how often this anime was recommended (?)
     */
    @Nullable
    @SerializedName("num_recommendations")
    public Integer recommendationCount;
}
