package io.github.shadow578.tenshi.mal.model;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

/**
 * a common image on MAL, eg. a poster of a {@link Anime} or a {@link User} profile picture
 */
public final class Image {
    /**
     * medium resolution image
     */
    @SerializedName("medium")
    @ColumnInfo(name = "medium")
    public String mediumUrl;

    /**
     * high resolution image
     */
    @SerializedName("large")
    @ColumnInfo(name = "large")
    public String largeUrl;
}
