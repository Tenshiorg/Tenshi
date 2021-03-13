package io.github.shadow578.tenshi.mal.model.type;

import com.google.gson.annotations.SerializedName;

/**
 * Anime ranking types for v2/anime/ranking endpoint
 */
public enum RankingType {
    /**
     * Top Anime Series
     */
    @SerializedName("all")
    All,

    /**
     * Top Airing Anime
     */
    @SerializedName("airing")
    Airing,

    /**
     * Top Upcoming Anime
     */
    @SerializedName("upcoming")
    Upcoming,

    /**
     * Top Anime TV Series
     */
    @SerializedName("tv")
    TV,

    /**
     * Top Anime OVA Series
     */
    @SerializedName("ova")
    OVA,

    /**
     * Top Anime Movies
     */
    @SerializedName("movie")
    Movie,

    /**
     * Top Anime Specials
     */
    @SerializedName("special")
    Special,

    /**
     * Top Anime by Popularity
     */
    @SerializedName("bypopularity")
    ByPopularity,

    /**
     * Top Favorited Anime
     */
    @SerializedName("favorite")
    Favorite
}
