package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.time.ZonedDateTime;

import io.github.shadow578.tenshi.mal.Data;

/**
 * a MAL user with attached statistics
 */
@Data
public final class User {
    /**
     * the users MAL id
     */
    @SerializedName("id")
    public int userID;

    /**
     * the user name
     */
    @Nullable
    public String name;

    /**
     * gender of this user
     */
    @Nullable
    public String gender;

    /**
     * when the user has its birthday
     */
    @Nullable
    public ZonedDateTime birthday;

    /**
     * where the user is located at
     */
    @Nullable
    public String location;

    /**
     * when the user joined MAL
     */
    @Nullable
    @SerializedName("joined_at")
    public ZonedDateTime joinedAt;

    /**
     * user profile picture
     */
    @Nullable
    @SerializedName("picture")
    public String profilePictureUrl;

    /**
     * anime watch statistics
     */
    @Nullable
    @SerializedName("anime_statistics")
    public UserStatistics statistics;
}
