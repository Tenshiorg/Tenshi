package io.github.shadow578.tenshi.mal.model.type;

import com.google.gson.annotations.SerializedName;

import io.github.shadow578.tenshi.mal.model.Anime;

/**
 * the age rating of a {@link Anime} or other media
 */
public enum ContentRating {
    /**
     * g - all ages
     */
    @SerializedName("g")
    AllAges,

    /**
     * pg - children
     */
    @SerializedName("pg")
    Children,

    /**
     * pg13 - teens 13+
     */
    @SerializedName("pg_13")
    Teens,

    /**
     * r - 17+ (violence and profanity
     */
    @SerializedName("r")
    ViolenceAndProfanity,

    /**
     * r+ - profanity and mild nudity
     */
    @SerializedName("r+")
    ProfanityAndMildNudity,

    /**
     * rx - hentai
     */
    @SerializedName("rx")
    LiterallyHentai
}
