package io.github.shadow578.tenshi.mal.model.type;

import com.google.gson.annotations.SerializedName;

/**
 * the season of a year, used in {@link io.github.shadow578.tenshi.mal.model.Season}
 */
public enum YearSeason {
    /**
     * Winter (January, February, March)
     */
    @SerializedName("winter")
    Winter,

    /**
     * Spring (April, May, June)
     */
    @SerializedName("spring")
    Spring,

    /**
     * Summer (July, August, September)
     */
    @SerializedName("summer")
    Summer,

    /**
     * Fall (October, November, December)
     */
    @SerializedName("fall")
    Fall
}
