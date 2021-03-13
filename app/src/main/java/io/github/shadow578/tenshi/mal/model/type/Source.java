package io.github.shadow578.tenshi.mal.model.type;

import com.google.gson.annotations.SerializedName;

/**
 * describes the original source of this anime/manga, eg. by what the story was inspired by / adapted from
 */
public enum  Source {
    /**
     * this is a original story
     */
    @SerializedName("original")
    Original,

    /**
     * story based on manga
     */
    @SerializedName("manga")
    Manga,

    /**
     * story based on a manhua
     */
    @SerializedName("manhua")
    Manhua,

    /**
     * story based on a 4- koma
     */
    @SerializedName("4_koma_manga")
    FourKoma,

    /**
     * story based on a web manga
     */
    @SerializedName("web_manga")
    WebManga,

    /**
     * story based on a novel
     */
    @SerializedName("novel")
    Novel,

    /**
     * story based on a light novel
     */
    @SerializedName("light_novel")
    LightNovel,

    /**
     * story based on a visual novel
     */
    @SerializedName("visual_novel")
    VisualNovel,

    /**
     * story based on a game
     */
    @SerializedName("game")
    Game,

    /**
     * story based on a book
     */
    @SerializedName("book")
    Book,

    /**
     * story based on music (?)
     */
    @SerializedName("music")
    Music,

    /**
     * story based on something else
     */
    @SerializedName("other")
    Other
}
