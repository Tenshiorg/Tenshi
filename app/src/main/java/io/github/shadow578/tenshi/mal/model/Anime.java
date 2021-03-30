package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.Nullable;

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
public final class Anime {
    /**
     * the id of this anime
     */
    @SerializedName("id")
    public int animeId;

    /**
     * synonyms for this animes title
     */
    @Nullable
    @SerializedName("alternative_titles")
    public TitleSynonyms titleSynonyms;

    /**
     * average duration (in seconds) of the episodes
     */
    @Nullable
    @SerializedName("average_episode_duration")
    public Integer averageEpisodeDuration;

    /**
     * information about when this anime broadcasts
     */
    @Nullable
    @SerializedName("broadcast")
    public BroadcastInfo broadcastInfo;

    /**
     * when this entry was created, ISO8601 datetime
     */
    @Nullable
    @SerializedName("created_at")
    public ZonedDateTime createdAt;

    /**
     * the date the anime ended, yyyy-mm-dd date
     */
    @Nullable
    @SerializedName("end_date")
    public LocalDate endDate;

    /**
     * genres of the anime
     */
    @Nullable
    public List<Genre> genres;

    /**
     * poster artwork of the anime
     */
    @Nullable
    @SerializedName("main_picture")
    public Image poster;

    /**
     * mean score for this anime
     */
    @Nullable
    @SerializedName("mean")
    public Double meanScore;

    /**
     * the type of this media
     */
    @Nullable
    @SerializedName("media_type")
    public MediaType mediaType;

    /**
     * how NSFW this anime is
     */
    @Nullable
    public NSFWRating nsfw;

    /**
     * the number of episodes
     */
    @Nullable
    @SerializedName("num_episodes")
    public Integer episodesCount;

    /**
     * how many users added this anime to their favorites
     */
    @Nullable
    @SerializedName("num_favorites")
    public Integer favoritesCount;

    /**
     * how many users added this anime to their list
     */
    @Nullable
    @SerializedName("num_list_users")
    public Integer listUsersCount;

    /**
     * how many users that voted (meanScore)
     */
    @Nullable
    @SerializedName("num_scoring_users")
    public Integer userVotesCount;

    /**
     * popularity rank of this anime
     */
    @Nullable
    public Integer popularity;

    /**
     * the global rank of this anime
     */
    @Nullable
    public Integer rank;

    /**
     * start date of this anime, yyyy-mm-dd date
     */
    @Nullable
    @SerializedName("start_date")
    public LocalDate startDate;

    /**
     * the season the anime started broadcasting
     */
    @Nullable
    @SerializedName("start_season")
    public Season startSeason;

    /**
     * the broadcasting status of this anime
     */
    @Nullable
    @SerializedName("status")
    public BroadcastStatus broadcastStatus;

    /**
     * synopsis of this anime
     */
    @Nullable
    public String synopsis;

    /**
     * original work that inspired this anime
     */
    @Nullable
    @SerializedName("source")
    public Source originalSource;

    /**
     * studios that worked on this anime
     */
    @Nullable
    public List<Studio> studios;

    /**
     * the canonical title of this anime
     */
    @Nullable
    public String title;

    /**
     * the last time this anime info was updated, ISO8601 datetime
     */
    @Nullable
    @SerializedName("updated_at")
    public ZonedDateTime lastUpdate;

    /**
     * status in the user list
     */
    @Nullable
    @SerializedName("my_list_status")
    public LibraryStatus userListStatus;

    /**
     * background story to this anime
     */
    @Nullable
    @SerializedName("background")
    public String backgroundStory;

    /**
     * a list of anime related to this anime
     */
    @Nullable
    @DataInclude(includeFields = {"media_type"})
    @SerializedName("related_anime")
    public List<RelatedMedia> relatedAnime;

    /**
     * a list of manga related to this anime
     */
    @Nullable
    @DataInclude(includeFields = {"media_type"})
    @SerializedName("related_manga")
    public List<RelatedMedia> relatedManga;

    /**
     * PG rating of this anime
     */
    @Nullable
    @SerializedName("rating")
    public ContentRating pgRating;

    /**
     * a list of pictures associated with this anime.
     * This includes the poster (main_picture), but also stuff like alternative pictures
     */
    @Nullable
    public List<Image> pictures;

    /**
     * a list of recommended anime and manga
     */
    @Nullable
    public List<RecommendedMedia> recommendations;

    /**
     * a list of opening themes this anime has
     */
    @Nullable
    @SerializedName("opening_themes")
    public List<Theme> openingThemes;

    /**
     * a list of ending themes this anime has
     */
    @Nullable
    @SerializedName("ending_themes")
    public List<Theme> endingThemes;
}
