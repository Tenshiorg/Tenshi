package io.github.shadow578.tenshi.mal.model;

import androidx.room.ColumnInfo;

import java.util.List;

/**
 * synonyms for the title of a {@link Anime}
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class TitleSynonyms {
    /**
     * the english title
     */
    @ColumnInfo(name = "en")
    public String en;

    /**
     * the japanese title
     */
    @ColumnInfo(name = "jp")
    public String jp;

    /**
     * a list of title synonyms
     */
    @ColumnInfo(name = "synonyms")
    public List<String> synonyms;
}
