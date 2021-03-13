package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.time.ZonedDateTime;

import io.github.shadow578.tenshi.mal.Data;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;

/**
 * the status of a {@link Anime} in the Library of a {@link User}
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
@Data
public class LibraryStatus {
    /**
     * list status (category)
     */
    public LibraryEntryStatus status;

    /**
     * the users comments on this anime
     */
    @Nullable
    public String comments;

    /**
     * is the user rewatching this anime?
     */
    @Nullable
    @SerializedName("is_rewatching")
    public Boolean isRewatching;

    /**
     * how many episodes are watched already
     */
    @Nullable
    @SerializedName("num_episodes_watched")
    public Integer watchedEpisodes;

    /**
     * how often this anime was rewatched
     */
    @Nullable
    @SerializedName("num_times_rewatched")
    public Integer rewatchCount;

    /**
     * the users rating for this anime
     */
    @Nullable
    public Integer score;

    /**
     * when this entry was last updated, ISO8601 datetime
     */
    @Nullable
    @SerializedName("updated_at")
    public ZonedDateTime lastUpdate;
}
