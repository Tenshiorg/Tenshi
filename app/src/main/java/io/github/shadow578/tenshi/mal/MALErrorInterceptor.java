package io.github.shadow578.tenshi.mal;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import io.github.shadow578.tenshi.lang.Consumer;
import io.github.shadow578.tenshi.mal.model.ErrorResponse;
import okhttp3.Interceptor;
import okhttp3.Response;

import static io.github.shadow578.tenshi.lang.LanguageUtils.notNull;

/**
 * interceptor for intercepting and calling back on error responses by MAL api
 */
public class MALErrorInterceptor implements Interceptor {
    private final Consumer<ErrorResponse> errorHandler;

    /**
     * initialize the interceptor
     * @param onError the callback for error responses
     */
    public MALErrorInterceptor(@Nullable Consumer<ErrorResponse> onError) {
        errorHandler = onError;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        // dont do anything on request, just intercept response
        final Response response = chain.proceed(chain.request());

        // log error object for unsuccessful responses
        if (!response.isSuccessful()) {
            // get error object from response
            ErrorResponse err = MalApiHelper.getError(response);

            // call error handler
            if (notNull(errorHandler) && notNull(err))
                errorHandler.invoke(err);
        }

        return response;
    }
}
