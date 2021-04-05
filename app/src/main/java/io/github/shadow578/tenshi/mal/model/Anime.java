package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import io.github.shadow578.tenshi.mal.Data;
import io.github.shadow578.tenshi.mal.DataInclude;
import io.github.shadow578.tenshi.mal.model.type.BroadcastStatus;
import io.github.shadow578.tenshi.mal.model.type.ContentRating;
import io.github.shadow578.tenshi.mal.model.type.MediaType;
import io.github.shadow578.tenshi.mal.model.type.NSFWRating;
import io.github.shadow578.tenshi.mal.model.type.Source;

/**
 * a anime on MAL
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
@Data
@Entity(tableName = "anime",
        indices = {
                @Index(value = "anime_id", unique = true)
        })
public final class Anime {
    /**
     * the id of this anime
     */
    @SerializedName("id")
    @ColumnInfo(name = "anime_id")
    @PrimaryKey
    public int animeId;

    /**
     * synonyms for this animes title
     */
    @Nullable
    @SerializedName("alternative_titles")
    @Embedded(prefix = "alternative_titles_")
    public TitleSynonyms titleSynonyms;

    /**
     * average duration (in seconds) of the episodes
     */
    @Nullable
    @SerializedName("average_episode_duration")
    @ColumnInfo(name = "average_episode_duration")
    public Integer averageEpisodeDuration;

    /**
     * information about when this anime broadcasts
     */
    @Nullable
    @SerializedName("broadcast")
    @Embedded(prefix = "broadcast_")
    public BroadcastInfo broadcastInfo;

    /**
     * when this entry was created, ISO8601 datetime
     */
    @Nullable
    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    public ZonedDateTime createdAt;

    /**
     * the date the anime ended, yyyy-mm-dd date
     */
    @Nullable
    @SerializedName("end_date")
    @ColumnInfo(name = "end_date")
    public LocalDate endDate;

    /**
     * genres of the anime
     */
    @Nullable
    @Ignore
    public List<Genre> genres;

    /**
     * poster artwork of the anime
     */
    @Nullable
    @SerializedName("main_picture")
    @Embedded(prefix = "main_picture_")
    public Image poster;

    /**
     * mean score for this anime
     */
    @Nullable
    @SerializedName("mean")
    @ColumnInfo(name = "mean")
    public Double meanScore;

    /**
     * the type of this media
     */
    @Nullable
    @SerializedName("media_type")
    @ColumnInfo(name = "media_type")
    public MediaType mediaType;

    /**
     * how NSFW this anime is
     */
    @Nullable
    @ColumnInfo(name = "nsfw")
    public NSFWRating nsfw;

    /**
     * the number of episodes
     */
    @Nullable
    @SerializedName("num_episodes")
    @ColumnInfo(name = "num_episodes")
    public Integer episodesCount;

    /**
     * how many users added this anime to their favorites
     */
    @Nullable
    @SerializedName("num_favorites")
    @ColumnInfo(name = "num_favorites")
    public Integer favoritesCount;

    /**
     * how many users added this anime to their list
     */
    @Nullable
    @SerializedName("num_list_users")
    @ColumnInfo(name = "num_list_users")
    public Integer listUsersCount;

    /**
     * how many users that voted (meanScore)
     */
    @Nullable
    @SerializedName("num_scoring_users")
    @ColumnInfo(name = "num_scoring_users")
    public Integer userVotesCount;

    /**
     * popularity rank of this anime
     */
    @Nullable
    @ColumnInfo(name = "popularity")
    public Integer popularity;

    /**
     * the global rank of this anime
     */
    @Nullable
    @ColumnInfo(name = "rank")
    public Integer rank;

    /**
     * start date of this anime, yyyy-mm-dd date
     */
    @Nullable
    @SerializedName("start_date")
    @ColumnInfo(name = "start_date")
    public LocalDate startDate;

    /**
     * the season the anime started broadcasting
     */
    @Nullable
    @SerializedName("start_season")
    @Embedded(prefix = "start_season_")
    public Season startSeason;

    /**
     * the broadcasting status of this anime
     */
    @Nullable
    @SerializedName("status")
    @ColumnInfo(name = "status")
    public BroadcastStatus broadcastStatus;

    /**
     * synopsis of this anime
     */
    @Nullable
    @ColumnInfo(name = "synopsis")
    public String synopsis;

    /**
     * original work that inspired this anime
     */
    @Nullable
    @SerializedName("source")
    @ColumnInfo(name = "source")
    public Source originalSource;

    /**
     * studios that worked on this anime
     */
    @Nullable
    @Ignore
    public List<Studio> studios;

    /**
     * the canonical title of this anime
     */
    @Nullable
    @ColumnInfo(name = "title")
    public String title;

    /**
     * the last time this anime info was updated, ISO8601 datetime
     */
    @Nullable
    @SerializedName("updated_at")
    @ColumnInfo(name = "updated_at")
    public ZonedDateTime lastUpdate;

    /**
     * status in the user list
     */
    @Nullable
    @SerializedName("my_list_status")
    @Embedded(prefix = "my_list_status_")
    public LibraryStatus userListStatus;

    /**
     * background story to this anime
     */
    @Nullable
    @SerializedName("background")
    @ColumnInfo(name = "background")
    public String backgroundStory;

    /**
     * a list of anime related to this anime
     */
    @Nullable
    @DataInclude(includeFields = {"media_type"})
    @SerializedName("related_anime")
    @Ignore
    public List<RelatedMedia> relatedAnime;

    /**
     * a list of manga related to this anime
     */
    @Nullable
    @DataInclude(includeFields = {"media_type"})
    @SerializedName("related_manga")
    @Ignore
    public List<RelatedMedia> relatedManga;

    /**
     * PG rating of this anime
     */
    @Nullable
    @SerializedName("rating")
    @ColumnInfo(name = "rating")
    public ContentRating pgRating;

    /**
     * a list of pictures associated with this anime.
     * This includes the poster (main_picture), but also stuff like alternative pictures
     */
    @Nullable
    @ColumnInfo(name = "pictures")
    public List<Image> pictures;

    /**
     * a list of recommended anime and manga
     */
    @Nullable
    @Ignore
    public List<RecommendedMedia> recommendations;

    /**
     * a list of opening themes this anime has
     */
    @Nullable
    @SerializedName("opening_themes")
    @Ignore
    public List<Theme> openingThemes;

    /**
     * a list of ending themes this anime has
     */
    @Nullable
    @SerializedName("ending_themes")
    @Ignore
    public List<Theme> endingThemes;
}
