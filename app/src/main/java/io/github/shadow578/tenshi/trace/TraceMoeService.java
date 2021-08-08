package io.github.shadow578.tenshi.trace;

import io.github.shadow578.tenshi.trace.model.QuotaInfo;
import io.github.shadow578.tenshi.trace.model.TraceResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * retrofit service for trace.moe's api
 */
public interface TraceMoeService {
    /**
     * search for a image.
     * you should probably not use this directly, but use {@link TraceAPI#search(byte[], Callback)} (or similar) instead
     *
     * @param body multipart body
     * @return the result
     */
    @POST("/search?anilistInfo")
    Call<TraceResponse> search(@Body MultipartBody body);

    /**
     * get the remaining quota
     *
     * @return the quota info
     */
    @GET("/me")
    Call<QuotaInfo> getQuota();
}
