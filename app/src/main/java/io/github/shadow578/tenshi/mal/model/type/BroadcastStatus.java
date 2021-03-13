package io.github.shadow578.tenshi.mal.model.type;

import com.google.gson.annotations.SerializedName;

/**
 * broadcasting status of a {@link io.github.shadow578.tenshi.mal.model.Anime}
 */
public enum BroadcastStatus {

    /**
     * a anime that finished airing
     */
    @SerializedName("finished_airing")
    FinishedAiring,

    /**
     * a anime that is currently airing
     */
    @SerializedName("currently_airing")
    CurrentlyAiring,

    /**
     * a anime that did not air yet
     */
    @SerializedName("not_yet_aired")
    NotYetAired,

    /**
     * a manga that is currently being published
     */
    @SerializedName("currently_publishing")
    CurrentlyPublishing,

    /**
     * a manga that concluded
     */
    @SerializedName("finished")
    Finished,

    /**
     * a manga that is currently being released, but not regularly (== is on hiatus)
     */
    @SerializedName("on_hiatus")
    OnHiatus,

    /**
     * a anime/manga that was discontinued
     */
    @SerializedName("discontinued")
    Discontinued
}
