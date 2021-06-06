package io.github.shadow578.tenshi.trace;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;

import io.github.shadow578.tenshi.BuildConfig;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.trace.model.TraceResponse;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;

/**
 * provides functions for working with the trace.moe api
 */
public class TraceAPI {
    /**
     * api endpoint of trace.moe
     */
    public static final String TRACE_API = "https://api.trace.moe/";

    /**
     * maximum image width for upload to trace.moe
     */
    private static final double MAX_IMAGE_WIDTH = 1280;

    /**
     * maximum image height for upload to trace.moe
     */
    private static final double MAX_IMAGE_HEIGHT = 720;

    /**
     * trace.moe api instance
     */
    private TraceMoeService trace;


    /**
     * search for a image on trace.moe.
     * wrapper for {@link #search(byte[], Callback)} that handles bitmap to byte[] conversion for you
     * the retrofit service will be initialized on demand
     *
     * @param image       the image to search for.
     *                    the bitmap is downscaled to {@value #MAX_IMAGE_WIDTH} x {@value #MAX_IMAGE_HEIGHT} for you if it is bigger to avoid uploading too big images to trace.moe.
     *                    {@link Bitmap#recycle()} is called on the image after the search automatically. this can be disabled by setting autoRecycle to false
     * @param callback    retrofit- style callback
     * @param autoRecycle should the input image be recycled automatically?
     *                    this only affects the bitmap passed to the function, the internally scaled bitmap will always be recycled as there's no way to access it otherwise
     */
    public void search(@NonNull Bitmap image, @NonNull Callback<TraceResponse> callback, boolean autoRecycle) {
        // scale the image if too big
        final Bitmap imgForSearch;
        int imgW = image.getWidth();
        int imgH = image.getHeight();
        if (imgW > MAX_IMAGE_WIDTH || imgH > MAX_IMAGE_HEIGHT) {
            // get scale factor
            final double scaleW = MAX_IMAGE_WIDTH / imgW;
            final double scaleH = MAX_IMAGE_HEIGHT / imgH;
            final double scale = Math.min(scaleW, scaleH);

            // scale down image
            imgW *= scale;
            imgH *= scale;
            imgForSearch = Bitmap.createScaledBitmap(image, imgW, imgH, true);

            // recycle original image
            if (autoRecycle)
                image.recycle();
        } else
            imgForSearch = image;

        // convert (scaled) image to bytes in PNG format
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        imgForSearch.compress(Bitmap.CompressFormat.PNG, 100, out);
        final byte[] imgData = out.toByteArray();

        // recycle image
        // always recycle the scaled image
        if (autoRecycle || !imgForSearch.equals(image))
            imgForSearch.recycle();

        // call search
        search(imgData, callback);
    }

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
