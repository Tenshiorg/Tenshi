package io.github.shadow578.tenshi.mal;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import io.github.shadow578.tenshi.util.Util;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * interceptor to handle caching on offline devices
 */
public class CacheInterceptor implements Interceptor {
    private final Context ctx;

    /**
     * initialize the interceptor
     * @param c the context to work in
     */
    public CacheInterceptor(Context c) {
        ctx = c;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        if (Util.getConnectionType(ctx) != Util.ConnectionType.None) {
            //We have a active internet connection:
            //allow loading of cached version up to 1 minute old (max-age)
            request = request.newBuilder()
                    .header("Cache-Control", "public, max-age=60")
                    .build();
        } else {
            //We don't have a active internet connection:
            //allow loading of a cached version up to 7 days old (max-stale)
            //also only load data that is already in cache (only-if-cached)
            request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=604800")
                    .build();
        }
        return chain.proceed(request);
    }
}
