package io.github.shadow578.tenshi.trace.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * a single result in a {@link TraceResponse}.
 * this assumes that 'anilistInfo' is added to the query (see https://soruly.github.io/trace.moe-api/#/docs?id=include-anilist-info)
 */
public final class TraceResult {

    /**
     * anilist info, as included by trace.moe
     */
    @SerializedName("anilist")
    public AnilistInfo anilistInfo;

    /**
     * filename this match was found in
     */
    @SerializedName("filename")
    public String fileName;

    /**
     * episode this scene is in
     */
    @SerializedName("episode")
    @Nullable
    public Integer episode;

    /**
     * start time of the scene, in seconds
     */
    @SerializedName("from")
    public double sceneStartSeconds;

    /**
     * end time of the scene, in seconds
     */
    @SerializedName("to")
    public double sceneEndSeconds;

    /**
     * similarity compared to the search image (0.0 - 1.0)
     */
    @SerializedName("similarity")
    public double similarity;

    /**
     * url to the preview video of the scene
     */
    @SerializedName("video")
    public String previewVideoUrl;

    /**
     * url to the preview image of the scene
     */
    @SerializedName("image")
    public String previewImageUrl;
}
