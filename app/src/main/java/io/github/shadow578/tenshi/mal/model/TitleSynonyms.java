package io.github.shadow578.tenshi.mal.model;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

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
    @SerializedName("en")
    public String en;

    /**
     * the japanese title
     */
    @ColumnInfo(name = "jp")
    @SerializedName(value = "ja", alternate = {"jp"})
    public String jp;

    /**
     * a list of title synonyms
     */
    @ColumnInfo(name = "synonyms")
    @SerializedName("synonyms")
    public List<String> synonyms;
}
