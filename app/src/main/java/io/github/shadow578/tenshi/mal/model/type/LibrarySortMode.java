package io.github.shadow578.tenshi.mal.model.type;

import com.google.gson.annotations.SerializedName;

/**
 * Sort mode for a User Library request
 */
public enum LibrarySortMode {
    /**
     * sort by title, ascending
     */
    @SerializedName("anime_title")
    anime_title,

    /**
     * sort by user assigned score, descending
     */
    @SerializedName("list_score")
    list_score,

    /**
     * sort by last updated date, descending
     */
    @SerializedName("list_updated_at")
    list_updated_at,

    /**
     * sort by anime start date, descending
     */
    @SerializedName("anime_start_date")
    anime_start_date,

    /**
     * sort by anime ID, ascending (Under Development)
     */
    @SerializedName("anime_id")
    anime_id
}
