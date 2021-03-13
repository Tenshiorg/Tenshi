package io.github.shadow578.tenshi.mal;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * interceptor to inject a Authorization header into requests
 */
public class AuthInterceptor implements Interceptor {

    /**
     * the token string to add as Authorization header, eg. "Bearer [TOKEN]"
     */
    @NonNull
    private final String token;

    /**
     * initialize the auth interceptor
     * @param type the type of the token supplied
     * @param token the token to add to requests
     */
    public AuthInterceptor(@NonNull String type, @NonNull String token)
    {
        this.token = type + " " + token;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request original = chain.request();
        Request modified = original.newBuilder()
                .header("Authorization", token)
                .build();
        return chain.proceed(modified);
    }
}
