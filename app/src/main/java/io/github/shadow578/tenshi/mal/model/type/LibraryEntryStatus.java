package io.github.shadow578.tenshi.mal.model.type;

import com.google.gson.annotations.SerializedName;

import io.github.shadow578.tenshi.ui.fragments.UserLibraryCategoryFragment;

/**
 * library status of a {@link io.github.shadow578.tenshi.mal.model.Anime}
 */
public enum LibraryEntryStatus {
    /**
     * currently watching this anime
     */
    @SerializedName("watching")
    Watching,

    /**
     * planning to watch this anime in the future
     */
    @SerializedName("plan_to_watch")
    PlanToWatch,

    /**
     * paused watching this anime, but may resume later
     */
    @SerializedName("on_hold")
    OnHold,

    /**
     * stopped watching this anime
     */
    @SerializedName("dropped")
    Dropped,

    /**
     * finished watching this anime
     */
    @SerializedName("completed")
    Completed,

    /**
     * all anime in the library.
     * This is not actually a status in the MAL api, but {@link UserLibraryCategoryFragment} has a check for it and handles it internally to display all anime.
     * Don't expect any anime to be in this category, and don't try to update a anime to this category!
     */
    All
}
