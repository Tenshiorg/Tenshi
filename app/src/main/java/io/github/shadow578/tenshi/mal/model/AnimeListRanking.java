package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * a paged list of {@link AnimeRankingItem} with {@link Paging}
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class AnimeListRanking {
    /**
     * items in the anime list
     */
    @NonNull
    @SerializedName("data")
    public List<AnimeRankingItem> items = new ArrayList<>();

    /**
     * pagination information
     */
    @Nullable
    public Paging paging;
}
