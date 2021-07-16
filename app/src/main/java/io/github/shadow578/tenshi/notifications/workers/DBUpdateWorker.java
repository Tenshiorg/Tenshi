package io.github.shadow578.tenshi.notifications.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Constraints;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.github.shadow578.tenshi.BuildConfig;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.db.TenshiDB;
import io.github.shadow578.tenshi.mal.AuthInterceptor;
import io.github.shadow578.tenshi.mal.CacheInterceptor;
import io.github.shadow578.tenshi.mal.MALErrorInterceptor;
import io.github.shadow578.tenshi.mal.MalService;
import io.github.shadow578.tenshi.mal.Urls;
import io.github.shadow578.tenshi.mal.model.Token;
import io.github.shadow578.tenshi.mal.model.UserLibraryEntry;
import io.github.shadow578.tenshi.mal.model.UserLibraryList;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.mal.model.type.LibrarySortMode;
import io.github.shadow578.tenshi.util.TenshiPrefs;
import io.github.shadow578.tenshi.util.converter.GSONLocalDateAdapter;
import io.github.shadow578.tenshi.util.converter.GSONLocalTimeAdapter;
import io.github.shadow578.tenshi.util.converter.GSONZonedDateTimeAdapter;
import io.github.shadow578.tenshi.util.converter.RetrofitEnumConverterFactory;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.async;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.concat;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.elvisEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrWhitespace;

/**
 * worker for updating the database entries
 */
public class DBUpdateWorker extends ListenableWorker {
    /**
     * get the constrains that are placed on the execution of this worker
     *
     * @param ctx context to work in
     * @return the constrains
     */
    @NonNull
    public static Constraints getConstrains(@NonNull Context ctx) {
        //TODO constrains configuration in props

        // set base constraints
        final Constraints.Builder constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true);

        // set network type constraint
        TenshiPrefs.init(ctx);
        if (TenshiPrefs.getBool(TenshiPrefs.Key.AllowNotificationUpdatesOnMeteredConnection, false)) {
            constraints.setRequiredNetworkType(NetworkType.UNMETERED);
        } else {
            constraints.setRequiredNetworkType(NetworkType.CONNECTED);
        }

        return constraints.build();
    }

    /**
     * @param ctx the context to run in
     * @return should this worker in in the given context?
     */
    public static boolean shouldEnable(@NonNull Context ctx) {
        return AiringAnimeWorker.shouldEnable(ctx) || RelatedAnimeWorker.shouldEnable(ctx);
    }

    /**
     * @param ctx context to run in
     * @return a list of all categories to check in this worker
     */
    @NonNull
    public static List<LibraryEntryStatus> getCategories(@NonNull Context ctx) {
        final HashSet<LibraryEntryStatus> categories = new HashSet<>();
        if (AiringAnimeWorker.shouldEnable(ctx))
            categories.addAll(AiringAnimeWorker.getCategories(ctx));

        if (RelatedAnimeWorker.shouldEnable(ctx))
            categories.addAll(RelatedAnimeWorker.getCategories(ctx));

        return new ArrayList<>(categories);
    }

    public DBUpdateWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * the MAL service instance
     */
    private MalService mal;

    /**
     * the database instance.
     */
    private TenshiDB db = null;

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            // initialize requirements
            if (!initMal()) {
                // MAL init failed
                completer.set(Result.failure());
            } else {
                // MAL initialized, fetch anime
                async(() -> {
                    try {
                        for (LibraryEntryStatus category : getCategories(getApplicationContext())) {
                            // prepare list
                            final List<UserLibraryEntry> entries = new ArrayList<>();

                            // fetch data
                            try {
                                fetchCategory(category, entries);
                            } catch (IOException e) {
                                //idk, print to log or something
                                e.printStackTrace();
                            }

                            // insert entries into DB (this updates them)
                            getDB().animeDB().insertLibraryAnime(entries);
                        }
                    } finally {
                        // all categories are done, mark work as finished
                        completer.set(Result.success());
                    }
                });
            }

            // this string is only used for debugging, as a 'label' of the future
            return "DBUpdateWorker_Future";
        });
    }

    /**
     * fetch all anime in a library category from MAL.
     * handles paging as well
     *
     * @param category the category to fetch
     * @param entries  the list of all entries found so far. entries from the response are added to this list
     * @throws IOException if the API call fails
     */
    private void fetchCategory(@NonNull LibraryEntryStatus category, @NonNull List<UserLibraryEntry> entries) throws IOException {
        // prepare fields
        final String ANIME_FIELDS = "id,title,alternative_titles,start_date,end_date,broadcast,status";
        final String FIELDS_TO_FETCH = concat(ANIME_FIELDS, ",related_anime{", ANIME_FIELDS, "}");

        // fetch a category from MAL, synchronous
        final Response<UserLibraryList> response = mal.getCurrentUserLibrary(category, LibrarySortMode.anime_title, FIELDS_TO_FETCH, 1)
                .execute();

        handleResponse(response, entries);
    }

    /**
     * fetch the next page of a user library request. called by {@link #handleResponse(Response, List)}
     *
     * @param nextPage the url of the next page to fetch
     * @param entries  the list of all entries found so far. entries from the response are added to this list
     * @throws IOException if the API call fails
     */
    private void fetchNextPage(@NonNull String nextPage, @NonNull List<UserLibraryEntry> entries) throws IOException {
        // fetch next page from MAL, synchronous
        final Response<UserLibraryList> response = mal.getUserAnimeListPage(nextPage)
                .execute();

        handleResponse(response, entries);
    }

    /**
     * handle a user library response.
     * if the response contains information for the next page, the next page is fetched using {@link #fetchNextPage(String, List)}.
     *
     * @param response the response to handle
     * @param entries  the list of all entries found so far. entries from the response are added to this list
     * @throws IOException if fetching of the next page fails
     */
    private void handleResponse(@NonNull Response<UserLibraryList> response, @NonNull List<UserLibraryEntry> entries) throws IOException {
        if (response.isSuccessful()) {
            // get response
            UserLibraryList library = response.body();
            if (notNull(library)) {
                // insert anime into the list
                entries.addAll(library.items);

                // load next page
                if (notNull(library.paging) && !nullOrWhitespace(library.paging.nextPage))
                    fetchNextPage(library.paging.nextPage, entries);
            }
        }
    }

    // region create MAL Api

    /**
     * create the MAL service retrofit instance.
     * only valid if isUserAuthenticated is true
     */
    private boolean initMal() {
        // init okhttp client
        final OkHttpClient client = createOkHttpClient();
        if (isNull(client))
            return false;

        // init retrofit
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Urls.API)
                .addConverterFactory(new RetrofitEnumConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .client(client)
                .build();

        // create service
        mal = retrofit.create(MalService.class);
        return true;
    }

    /**
     * create a OKHttp client with the required interceptors (for auth, ...)
     * On DEBUG builds, a logging interceptor is added aswell
     *
     * @return the OKHttp client instance
     */
    private OkHttpClient createOkHttpClient() {
        // load token from prefs
        TenshiPrefs.init(getApplicationContext());
        final Token tokenObj = TenshiPrefs.getObject(TenshiPrefs.Key.AuthToken, Token.class, null);
        if (isNull(tokenObj))
            return null;

        // get token and token type
        final String tokenType = elvisEmpty(tokenObj.type, "Bearer");
        final String token = tokenObj.token;
        if (nullOrEmpty(token))
            return null;

        // prepare interceptors
        final AuthInterceptor authInterceptor = new AuthInterceptor(tokenType, token);
        final CacheInterceptor cacheInterceptor = new CacheInterceptor(getApplicationContext());
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        // create MAL error interceptor to give info on api errors
        final MALErrorInterceptor errorInterceptor = new MALErrorInterceptor(
                err -> Log.e("Tenshi", fmt("error in MAL request: %s %s (hint: %s)", err.error, err.message, err.hint)));

        // create OkHttp client
        return new OkHttpClient.Builder()
                .cache(getCache())
                .addInterceptor(authInterceptor)
                .addInterceptor(cacheInterceptor)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(errorInterceptor)
                .build();
    }

    /**
     * create a cache for the OKHttp client
     *
     * @return the cache
     */
    private Cache getCache() {
        long cacheSize = 50 * 1024 * 1024; // 50MiB
        return new Cache(getApplicationContext().getCacheDir(), cacheSize);
    }

    /**
     * @return the global GSON instance, with additional adapters
     */
    @NonNull
    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new GSONLocalDateAdapter().nullSafe())
                .registerTypeAdapter(LocalTime.class, new GSONLocalTimeAdapter().nullSafe())
                .registerTypeAdapter(ZonedDateTime.class, new GSONZonedDateTimeAdapter().nullSafe())
                .create();

    }
    // endregion

    /**
     * get the tenshi anime database instance.
     * if the app is not running ({@link TenshiApp#getDB()} not possible) this initializes the database on the first call.
     *
     * @return the database instance
     */
    @NonNull
    private TenshiDB getDB() {
        if (notNull(db))
            return db;

        // try to get database from TenshiApp first
        if (notNull(TenshiApp.INSTANCE)) {
            db = TenshiApp.getDB();
            return db;
        }

        // app not running, create on demand
        db = TenshiDB.create(getApplicationContext());
        return db;
    }
}
