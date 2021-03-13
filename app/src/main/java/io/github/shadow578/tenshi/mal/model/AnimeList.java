package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * a paged list of {@link Anime} with {@link Paging}
 */
public final class AnimeList {
    /**
     * the anime in the list
     */
    @NonNull
    @SerializedName("data")
    public List<AnimeListItem> items = new ArrayList<>();

    /**
     * pagination info
     */
    @Nullable
    public Paging paging;
}
