package io.github.shadow578.tenshi.mal.model.type;

import com.google.gson.annotations.SerializedName;

/**
 * describes how a {@link io.github.shadow578.tenshi.mal.model.RelatedMedia} is related to a {@link io.github.shadow578.tenshi.mal.model.Anime}
 */
public enum RelationType {
    /**
     * this is a prequel to the other anime
     */
    @SerializedName("prequel")
    Prequel,

    /**
     * this is a sequel to the other anime
     */
    @SerializedName("sequel")
    Sequel,

    /**
     * this is a summary of the other anime
     */
    @SerializedName("summary")
    Summary,

    /**
     * this is a alternative version of the other anime
     */
    @SerializedName("alternative_version")
    AlternativeVersion,

    /**
     * this anime has a alternative setting to the other anime
     */
    @SerializedName("alternative_setting")
    AlternativeSetting,

    /**
     * this is a spin- off of the other anime
     */
    @SerializedName("spin_off")
    SpinOff,

    /**
     * this is a side- story of the other anime
     */
    @SerializedName("side_story")
    SideStory,

    /**
     * this is the parent story of the other anime
     */
    @SerializedName("parent_story")
    ParentStory,

    /**
     * this is the full story of a (side- story) anime
     */
    @SerializedName("full_story")
    FullStory,

    /**
     * this is a adaption of the other anime
     */
    @SerializedName("adaptation")
    Adaptation,

    /**
     * a character of the other anime (often MC) is in this anime
     */
    @SerializedName("character")
    Character,

    /**
     * a different relation
     */
    @SerializedName("other")
    Other
}
