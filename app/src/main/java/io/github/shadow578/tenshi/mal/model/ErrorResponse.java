package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Common error response of the MAL Api.
 * Essentially, this is the response body if a request is not successful
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class ErrorResponse {
    /**
     * the error
     */
    @NonNull
    public String error = "";

    /**
     * the error message
     */
    @NonNull
    public String message = "";

    /**
     * a hint on why we received this error
     */
    @Nullable
    public String hint;
}
