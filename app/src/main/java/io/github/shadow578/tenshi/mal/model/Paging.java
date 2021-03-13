package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * paging information for list responses
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class Paging {
    /**
     * the next page url
     */
    @SerializedName("next")
    @Nullable
    public String nextPage;

    /**
     * the previous page url
     */
    @Nullable
    @SerializedName("previous")
    public String previousPage;
}
