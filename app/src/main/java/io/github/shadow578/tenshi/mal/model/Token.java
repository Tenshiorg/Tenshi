package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * MAL OAuth token response
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class Token {
    /**
     * type of this token
     */
    @Nullable
    @SerializedName("token_type")
    public String type;

    /**
     * in how many seconds this token expires
     */
    @Nullable
    @SerializedName("expires_in")
    public Integer expiresIn;

    /**
     * the access token
     */
    @Nullable
    @SerializedName("access_token")
    public String token;

    /**
     * the refresh token
     */
    @Nullable
    @SerializedName("refresh_token")
    public String refreshToken;
}
