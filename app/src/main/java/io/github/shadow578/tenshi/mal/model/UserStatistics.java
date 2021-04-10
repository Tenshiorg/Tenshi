package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

import io.github.shadow578.tenshi.mal.Data;

/**
 * anime stats for a {@link User}
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
@Data
public final class UserStatistics {
    /**
     * how many library entries have the status watching
     */
    @Nullable
    @SerializedName("num_items_watching")
    @ColumnInfo(name = "num_items_watching")
    public Integer libraryWatchingCount;

    /**
     * how many library entries have the status completed
     */
    @Nullable
    @SerializedName("num_items_completed")
    @ColumnInfo(name = "num_items_completed")
    public Integer libraryCompletedCount;

    /**
     * how many library entries have the status on hold
     */
    @Nullable
    @SerializedName("num_items_on_hold")
    @ColumnInfo(name = "num_items_on_hold")
    public Integer libraryOnHoldCount;

    /**
     * how many library entries have the status dropped
     */
    @Nullable
    @SerializedName("num_items_dropped")
    @ColumnInfo(name = "num_items_dropped")
    public Integer libraryDroppedCount;

    /**
     * how many library entries have the status plan to watch
     */
    @Nullable
    @SerializedName("num_items_plan_to_watch")
    @ColumnInfo(name = "num_items_plan_to_watch")
    public Integer libraryPlanToWatchCount;

    /**
     * total number of library entries
     */
    @Nullable
    @SerializedName("num_items")
    @ColumnInfo(name = "num_items")
    public Integer libraryTotalCount;

    /**
     * how many days of anime this user watched in total
     */
    @Nullable
    @SerializedName("num_days_watched")
    @ColumnInfo(name = "num_days_watched")
    public Double totalDaysWatched;

    /**
     * how many days all library items with status watching add up to
     */
    @Nullable
    @SerializedName("num_days_watching")
    @ColumnInfo(name = "num_days_watching")
    public Double totalDaysWatching;

    /**
     * how many days all library items with status completed add up to
     */
    @Nullable
    @SerializedName("num_days_completed")
    @ColumnInfo(name = "num_days_completed")
    public Double totalDaysCompleted;

    /**
     * how many days all library items with status on hold add up to
     */
    @Nullable
    @SerializedName("num_days_on_hold")
    @ColumnInfo(name = "num_days_on_hold")
    public Double totalDaysOnHold;

    /**
     * how many days all library items with dropped watching add up to
     */
    @Nullable
    @SerializedName("num_days_dropped")
    @ColumnInfo(name = "num_days_dropped")
    public Double totalDaysDropped;

    /**
     * total number of days wasted
     */
    @Nullable
    @SerializedName("num_days")
    @ColumnInfo(name = "num_days")
    public Double totalDaysWasted;

    /**
     * total number of episodes watched
     */
    @Nullable
    @SerializedName("num_episodes")
    @ColumnInfo(name = "num_episodes")
    public Integer totalEpisodesWatched;

    /**
     * total number of rewatched episoded
     */
    @Nullable
    @SerializedName("num_times_rewatched")
    @ColumnInfo(name = "num_times_rewatched")
    public Integer totalEpisodedRewatched;

    /**
     * average score of all rated anime
     */
    @Nullable
    @SerializedName("mean_score")
    @ColumnInfo(name = "mean_score")
    public Double meanScore;
}
