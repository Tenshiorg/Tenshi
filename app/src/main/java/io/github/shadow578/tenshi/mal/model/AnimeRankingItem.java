package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import io.github.shadow578.tenshi.mal.model.type.RankingType;

/**
 * a {@link Anime} with ranking info attached to it
 */
public final class AnimeRankingItem {

    /**
     * the anime ranked
     */
    @SerializedName("node")
    @NonNull
    public Anime anime = new Anime();

    /**
     * ranking information
     */
    @Nullable
    public Ranking ranking;

    /**
     * ranking type of the original call
     */
    @Nullable
    public RankingType ranking_type;
}
