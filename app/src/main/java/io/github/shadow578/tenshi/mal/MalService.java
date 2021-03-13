package io.github.shadow578.tenshi.mal;

import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.AnimeList;
import io.github.shadow578.tenshi.mal.model.AnimeListRanking;
import io.github.shadow578.tenshi.mal.model.LibraryStatus;
import io.github.shadow578.tenshi.mal.model.User;
import io.github.shadow578.tenshi.mal.model.UserLibraryList;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.mal.model.type.LibrarySortMode;
import io.github.shadow578.tenshi.mal.model.type.RankingType;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * main MAL api endpoints
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public interface MalService {
    // region /v2/users

    /**
     * gets the currently logged user
     *
     * @param fields fields to include, from User object
     * @return the user object call
     */
    @GET("/v2/users/@me")
    Call<User> getCurrentUser(@Query("fields") String fields);

    /**
     * get a specific user by id
     *
     * @param userId the id of the user to get
     * @param fields fields to include, from User object
     * @return the user object call
     */
    @GET("/v2/users/{id}")
    Call<User> getUser(@Path("id") int userId, @Query("fields") String fields);
    //endregion

    //region /v2/anime

    /**
     * get a specific anime by id
     *
     * @param id     the anime id
     * @param fields fields to include, from Anime object
     * @return the anime object call
     */
    @GET("/v2/anime/{id}")
    Call<Anime> getAnime(@Path("id") int id, @Query("fields") String fields);

    /**
     * search mal for anime
     *
     * @param searchQuery the search query
     * @param limit       how many results to return
     * @param nsfw        include nsfw results? (0=no, 1=yes)
     * @param fields      fields to include, from Anime object
     * @return a list of anime
     */
    @GET("/v2/anime")
    Call<AnimeList> searchAnime(@Query("q") String searchQuery,
                                @Query("limit") Integer limit,
                                @Query("nsfw") Integer nsfw,
                                @Query("fields") String fields);


    /**
     * get ranking anime by type
     *
     * @param rankingType the type of ranking to get
     * @param fields      the fields to include, of the Anime object
     * @param limit       maximum number of items to return
     * @param nsfw        include nsfw results? (0=no, 1=yes)
     * @return a list of ranking anime
     */
    @GET("/v2/anime/ranking")
    Call<AnimeListRanking> getAnimeRanking(@Query("ranking_type") RankingType rankingType,
                                           @Query("fields") String fields,
                                           @Query("limit") Integer limit,
                                           @Query("nsfw") Integer nsfw);

    /**
     * get suggested anime for the current user
     *
     * @param limit  maximum number of items to return
     * @param fields the fields to include, of the Anime object
     * @return a list of recommended anime
     */
    @GET("/v2/anime/suggestions")
    Call<AnimeList> getAnimeRecommendations(@Query("limit") Integer limit,
                                            @Query("fields") String fields);


    /**
     * generic call to get a AnimeList from a paginated url.
     * useable for all responses of type AnimeList that use pagination, like recommended anime and anime search
     *
     * @param url the url of the page to get
     * @return the anime list object
     */
    @GET
    Call<AnimeList> getAnimeList(@Url String url);
    //endregion

    //region /v2/users/<id>/animelist

    /**
     * get user library entries
     *
     * @param status status filter for entries
     * @param sort   sort mode
     * @param fields fields to include, from UserLibraryEntry object
     * @param nsfw   include nsfw entries? (0=no, 1=yes)
     * @return the users anime list
     */
    @GET("/v2/users/@me/animelist")
    Call<UserLibraryList> getCurrentUserLibrary(@Query("status") LibraryEntryStatus status,
                                                @Query("sort") LibrarySortMode sort,
                                                @Query("fields") String fields,
                                                @Query("nsfw") Integer nsfw);

    /**
     * get user library entries
     *
     * @param userID the user to get entries of
     * @param status status filter for entries
     * @param sort   sort mode
     * @param fields fields to include, from UserLibraryEntry object
     * @param nsfw   include nsfw entries? (0=no, 1=yes)
     * @return the users anime list
     */
    @GET("/v2/users/{id}/animelist")
    Call<UserLibraryList> getUserLibrary(@Path("id") int userID,
                                         @Query("status") LibraryEntryStatus status,
                                         @Query("sort") LibrarySortMode sort,
                                         @Query("fields") String fields,
                                         @Query("nsfw") Integer nsfw);


    /**
     * get a page of the user library.
     * use urls from pagination
     *
     * @param url the url of the page to get
     * @return the users anime list page
     */
    @GET
    Call<UserLibraryList> getUserAnimeListPage(@Url String url);


    /**
     * update a library entry of the current user
     *
     * @param animeId         the anime library entry to update
     * @param status          the updated status. null if not changed
     * @param score           the update score. null if not changed, 0 to remove rating
     * @param watchedEpisodes number of episodes watched. null if not changed
     * @return the updated library status entry
     */
    @FormUrlEncoded
    @PATCH("/v2/anime/{id}/my_list_status")
    Call<LibraryStatus> updateListEntry(@Path("id") int animeId,
                                        @Field("status") LibraryEntryStatus status,
                                        @Field("score") Integer score,
                                        @Field("num_watched_episodes") Integer watchedEpisodes);

    /**
     * remove a library entry of the current user
     *
     * @param animeId the anime library entry to delete
     * @return nothing
     */
    @DELETE("/v2/anime/{id}/my_list_status")
    Call<Void> deleteListEntry(@Path("id") int animeId);
    //endregion
}
