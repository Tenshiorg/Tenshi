package io.github.shadow578.tenshi.trace.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * result of a image trace on trace.moe, assuming
 * that 'anilistInfo' is added to the query (see https://soruly.github.io/trace.moe-api/#/docs?id=include-anilist-info)
 */
public final class TraceResponse {

    /**
     * total number of frames searched.
     * only valid if no error, otherwise null
     */
    @SerializedName("frameCount")
    @Nullable
    public Long totalFramesSearched;

    /**
     * error message, if any
     */
    @SerializedName("error")
    @Nullable
    public String errorMessage;

    /**
     * results of this trace.
     * only valid if no error, otherwise null
     */
    @SerializedName("result")
    @Nullable
    public List<TraceResult> results;
}
