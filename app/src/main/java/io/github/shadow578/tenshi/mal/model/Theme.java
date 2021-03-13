package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;

/**
 * a Theme of a {@link Anime}.
 * Theme as in OP / ED. You know, music and stuff
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class Theme {
    /**
     * the id of this theme
     */
    public int id;

    /**
     *  the anime this theme is related to
     */
    @Nullable
    public Integer anime_id;

    /**
     * the text of this theme, commonly this is the (song) title
     */
    @Nullable
    public  String text;
}
