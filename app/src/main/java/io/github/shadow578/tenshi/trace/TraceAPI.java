package io.github.shadow578.tenshi.trace;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;

import io.github.shadow578.tenshi.BuildConfig;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.trace.model.QuotaInfo;
import io.github.shadow578.tenshi.trace.model.TraceResponse;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;

/**
 * provides functions for working with the trace.moe api
 */
public class TraceAPI {

    // region experimentation / testing

    /**
     * test TraceAPI
     * TODO test function, remove asap
     *
     * @param c application context
     */
    public static void test(Context c) {
        // create api instance and init
        final TraceAPI trace = new TraceAPI();

        // quota test
        trace.getService().getQuota().enqueue(new Callback<QuotaInfo>() {
            @Override
            public void onResponse(Call<QuotaInfo> call, Response<QuotaInfo> response) {
                QuotaInfo q = response.body();
                Log.i("Trace", String.format("Quota: id= %s, used %d of %d", q.id, q.quotaUsed, q.quotaTotal));
            }

            @Override
            public void onFailure(Call<QuotaInfo> call, Throwable t) {
                Log.e("Trace", "failed: " + t.toString());
                t.printStackTrace();
            }
        });

        // search test
        final byte[] image = downloadDemoImage();
        Log.i("Trace", "size: " + image.length);
        trace.search(image, new Callback<TraceResponse>() {
            @Override
            public void onResponse(Call<TraceResponse> call, Response<TraceResponse> response) {

                if (response.isSuccessful()) {
                    TraceResponse r = response.body();
                    Log.i("Trace", r.results.get(0).fileName);

                } else {
                    try {
                        Log.i("Trace", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<TraceResponse> call, Throwable t) {
                Log.e("Trace", "failed: " + t.toString());
                t.printStackTrace();
            }
        });
    }

    /**
     * download the demo image from the trace.moe documentation
     * TODO test function, remove asap
     *
     * @return image bytes, or null if failed
     */
    private static byte[] downloadDemoImage() {
        try {
            final Request fr = new Request.Builder()
                    .url("https://raw.githubusercontent.com/soruly/trace.moe/master/demo.jpg")
                    .build();
            final okhttp3.Response r = new OkHttpClient.Builder()
                    .build()
                    .newCall(fr)
                    .execute();

            return r.body().bytes();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

//endregion

    /**
     * api endpoint of trace.moe
     */
    public static final String TRACE_API = "https://api.trace.moe/";

    /**
     * trace.moe api instance
     */
    private TraceMoeService trace;

    /**
     * search for a image on trace.moe.
     * wrapper for {@link TraceMoeService#search(MultipartBody)} that handles the multipart stuff for you
     * the retrofit service will be initialized on demand
     *
     * @param image    the image data to search for
     * @param callback retrofit- style callback
     */
    public void search(@NonNull byte[] image, @NonNull Callback<TraceResponse> callback) {
        // create multipart body
        final MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM) // trace.moe requires Content-Type to be multipart/form-data
                .addFormDataPart("image", "image", RequestBody.create(image)) // name AND filename are required too
                .build();

        // enqueue the search
        getService().search(body).enqueue(callback);
    }

    /**
     * get the trace.moe retrofit service.
     * the retrofit service will be initialized on demand
     *
     * @return the service instance
     */
    @NonNull
    public TraceMoeService getService() {
        initOnDemand();
        return trace;
    }

    /**
     * initialize the trace.moe api service, if not already initialized
     */
    private void initOnDemand() {
        if (isNull(trace)) {
            // create retrofit
            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TRACE_API)
                    //.addConverterFactory(new RetrofitEnumConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create(getGson()))
                    .client(createHttpClient())
                    .build();

            // create service
            trace = retrofit.create(TraceMoeService.class);
        }
    }

    /**
     * create the gson instance for the trace.moe api.
     * this uses the same as MAL
     *
     * @return the gson to use for trace.moe
     */
    @NonNull
    private Gson getGson() {
        return TenshiApp.getGson();
    }

    /**
     * create a http client for request to trace.moe.
     * best practice would be to use only one client for the whole app, but this would
     * possibly mean sending the MAL auth key to trace.moe, which i don't want.
     *
     * @return the OkHttp client instance to use for trace.moe
     */
    @NonNull
    private OkHttpClient createHttpClient() {
        // prepare interceptors
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        // create OkHttp client
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

}
