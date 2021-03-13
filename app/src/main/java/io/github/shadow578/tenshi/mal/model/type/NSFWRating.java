package io.github.shadow578.tenshi.mal.model.type;

import com.google.gson.annotations.SerializedName;

/**
 * a rating for how NSFW a anime is
 */
public enum NSFWRating {
    /**
     * completely SFW
     */
    @SerializedName("white")
    White,

    /**
     * kinda NSFW
     */
    @SerializedName("gray")
    Gray,

    /**
     * full- on NSFW
     */
    @SerializedName("black")
    Black
}
