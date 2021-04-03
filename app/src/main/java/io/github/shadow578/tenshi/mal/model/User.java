package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.time.ZonedDateTime;

import io.github.shadow578.tenshi.mal.Data;

/**
 * a MAL user with attached statistics
 */
@Entity(tableName = "users",
        indices = {
        @Index(value = "user_id", unique = true)
})
@Data
public final class User {
    /**
     * the users MAL id
     */
    @SerializedName("id")
    @ColumnInfo(name = "user_id")
    @PrimaryKey
    public int userID;

    /**
     * the user name
     */
    @Nullable
    @ColumnInfo(name = "name")
    public String name;

    /**
     * gender of this user
     */
    @Nullable
    @ColumnInfo(name = "gender")
    public String gender;

    /**
     * when the user has its birthday
     */
    @Nullable
    @ColumnInfo(name = "birthday")
    public ZonedDateTime birthday;

    /**
     * where the user is located at
     */
    @Nullable
    @ColumnInfo(name = "location")
    public String location;

    /**
     * when the user joined MAL
     */
    @Nullable
    @SerializedName("joined_at")
    @ColumnInfo(name = "joined_at")
    public ZonedDateTime joinedAt;

    /**
     * user profile picture
     */
    @Nullable
    @SerializedName("picture")
    @ColumnInfo(name = "picture")
    public String profilePictureUrl;

    /**
     * anime watch statistics
     */
    @Nullable
    @SerializedName("anime_statistics")
    @Embedded(prefix = "anime_statistics")
    public UserStatistics statistics;
}
