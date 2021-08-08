package io.github.shadow578.tenshi.trace.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * anilist api response, as returned by trace.moe api
 */
public final class AnilistInfo {

    /**
     * anilist id of this anime
     */
    @SerializedName("id")
    public int anilistId;

    /**
     * MAL id of this anime
     */
    @SerializedName("idMal")
    public int malId;

    /**
     * anime title info
     */
    @SerializedName("title")
    public AnilistTitles titles;

    /**
     * title synonyms
     */
    @SerializedName("synonyms")
    public List<String> synonyms = new ArrayList<>();

    /**
     * is this anime mature content / NSFW?
     */
    @SerializedName("isAdult")
    public boolean isAdult;
}
