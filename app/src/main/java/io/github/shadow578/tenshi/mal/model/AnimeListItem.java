package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * wrapper for a {@link Anime}, because MAL requires that extra step -_-
 */
public final class AnimeListItem {
    @NonNull
    @SerializedName("node")
    public Anime anime = new Anime();
}
