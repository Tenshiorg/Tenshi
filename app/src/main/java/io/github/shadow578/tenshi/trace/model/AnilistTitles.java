package io.github.shadow578.tenshi.trace.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * anilist title info, as returned by trace.moe api
 */
public final class AnilistTitles {

    /**
     * native (japanese) title of the anime
     */
    @SerializedName("native")
    @Nullable
    public String nativeTitle;

    /**
     * romanji title of the anime
     */
    @SerializedName("romanji")
    @Nullable
    public String romanjiTitle;

    /**
     * (translated) english title of the anime
     */
    @SerializedName("english")
    @Nullable
    public String englishTitle;
}
