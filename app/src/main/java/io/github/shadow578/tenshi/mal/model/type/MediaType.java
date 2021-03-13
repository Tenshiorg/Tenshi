package io.github.shadow578.tenshi.mal.model.type;

import com.google.gson.annotations.SerializedName;

/**
 * media type of a {@link io.github.shadow578.tenshi.mal.model.Anime}
 */
public enum MediaType {
    /**
     * a "normal" TV anime seriaes
     */
    @SerializedName("tv")
    TV,

    /**
     * anime that was produced and (initially) released on video
     */
    @SerializedName("ova")
    OVA,

    /**
     * a full anime movie
     */
    @SerializedName("movie")
    Movie,

    /**
     * a anime special, often only few episodes
     */
    @SerializedName("special")
    Special,

    /**
     * anime that was (initially) released only on the internet
     */
    @SerializedName("ona")
    ONA,

    /**
     *
     */
    @SerializedName("music")
    Music,

    /**
     * a manga
     */
    @SerializedName("manga")
    Manga,

    /**
     * a manga one- shot (only one chapter)
     */
    @SerializedName("one_shot")
    OneShot,

    /**
     * manga sub-type (?)
     */
    @SerializedName("manhwa")
    Manhwa,

    /**
     * manga sub-type (?)
     */
    @SerializedName("manhua")
    Manhua,

    /**
     * a full novel
     */
    @SerializedName("novel")
    Novel,

    /**
     * a light novel
     */
    @SerializedName("light_novel")
    LightNovel,

    /**
     * a doujinshi (self- published manga)
     */
    @SerializedName("doujinshi")
    Doujinshi,

    /**
     * any other media type
     */
    @SerializedName("unknown")
    Unknown
}
