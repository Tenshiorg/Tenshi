package io.github.shadow578.tenshi;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import io.github.shadow578.tenshi.db.TenshiDB;
import io.github.shadow578.tenshi.extensionslib.content.ContentAdapterManager;
import io.github.shadow578.tenshi.mal.AuthInterceptor;
import io.github.shadow578.tenshi.mal.CacheInterceptor;
import io.github.shadow578.tenshi.mal.MALErrorInterceptor;
import io.github.shadow578.tenshi.mal.MalApiHelper;
import io.github.shadow578.tenshi.mal.MalService;
import io.github.shadow578.tenshi.mal.Urls;
import io.github.shadow578.tenshi.mal.model.Token;
import io.github.shadow578.tenshi.ui.MainActivity;
import io.github.shadow578.tenshi.ui.SearchActivity;
import io.github.shadow578.tenshi.ui.onboarding.OnboardingActivity;
import io.github.shadow578.tenshi.util.TenshiPrefs;
import io.github.shadow578.tenshi.util.converter.GSONLocalDateAdapter;
import io.github.shadow578.tenshi.util.converter.GSONLocalTimeAdapter;
import io.github.shadow578.tenshi.util.converter.GSONZonedDateTimeAdapter;
import io.github.shadow578.tenshi.util.converter.RetrofitEnumConverterFactory;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.async;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.elvisEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.listOf;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrWhitespace;

/**
 * Tenshi Core logic
 */
public class TenshiApp extends Application {
    /**
     * Singleton instance
     */
    public static TenshiApp INSTANCE;

    /**
     * global gson instance, with extra adapters
     */
    private Gson gson;

    /**
     * retrofit MAL api
     */
    private MalService mal;

    /**
     * the MAL auth token currently in use
     */
    private Token token;

    /**
     * are we currently refreshing the token?
     * used to prevent multiple refreshes to start at the same time
     */
    private boolean currentlyRefreshingToken = false;

    /**
     * manager for content adapters
     */
    private ContentAdapterManager contentAdapterManager;

    /**
     * offline database of the app
     */
    private TenshiDB database;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        // create app shortcuts
        initAppShortcuts();

        // auth with MAL
        TenshiPrefs.init(getApplicationContext());
        tryAuthInit();

        // init database and start cleanup
        database = TenshiDB.create(getApplicationContext());
        cleanupDatabase();

        // init and find content adapters
        contentAdapterManager = new ContentAdapterManager(getApplicationContext(), new ContentAdapterManager.IPersistentStorageProvider() {
            @NonNull
            @Override
            public String getPersistentStorage(@NonNull String uniqueName, int animeId) {
                return getDB().contentAdapterDB().getPersistentStorage(animeId, uniqueName);
            }

            @Override
            public void setPersistentStorage(@NonNull String uniqueName, int animeId, @NonNull String persistentStorage) {
                getDB().contentAdapterDB().setPersistentStorage(animeId, uniqueName, persistentStorage);
            }
        });
        contentAdapterManager.discoverAndInit(false);
        contentAdapterManager.addOnDiscoveryEndCallback(p
                -> Log.i("Tenshi", fmt("Discovery finished with %d content adapters found", contentAdapterManager.getAdapterCount())));
    }

    /**
     * dynamically initialize the "static" app shortcuts
     * because debug builds use a different app package, this is way less of a pain than using xml
     */
    private void initAppShortcuts() {
        // create shortcut infos
        // home
        final ShortcutInfoCompat scHome = new ShortcutInfoCompat.Builder(this, "TENSHI_STATIC_HOME")
                .setShortLabel(getString(R.string.main_nav_title_home))
                .setIcon(IconCompat.createWithResource(this, R.drawable.ic_shortcut_home_48))
                .setIntent(new Intent(this, MainActivity.class)
                        .setAction(MainActivity.Section.Home.name()))
                .build();

        // library
        final ShortcutInfoCompat scLibrary = new ShortcutInfoCompat.Builder(this, "TENSHI_STATIC_LIBRARY")
                .setShortLabel(getString(R.string.main_nav_title_anime_list))
                .setIcon(IconCompat.createWithResource(this, R.drawable.ic_shortcut_anime_48))
                .setIntent(new Intent(this, MainActivity.class)
                        .setAction(MainActivity.Section.Library.name()))
                .build();

        // profile
        final ShortcutInfoCompat scProfile = new ShortcutInfoCompat.Builder(this, "TENSHI_STATIC_PROFILE")
                .setShortLabel(getString(R.string.main_nav_title_profile))
                .setIcon(IconCompat.createWithResource(this, R.drawable.ic_shortcut_profile_48))
                .setIntent(new Intent(this, MainActivity.class)
                        .setAction(MainActivity.Section.Profile.name()))
                .build();

        // search
        final ShortcutInfoCompat scSearch = new ShortcutInfoCompat.Builder(this, "TENSHI_STATIC_SEARCH")
                .setShortLabel(getString(R.string.main_nav_title_search))
                .setIcon(IconCompat.createWithResource(this, R.drawable.ic_shortcut_search_48))
                .setIntent(new Intent(this, SearchActivity.class)
                        .setAction(Intent.ACTION_VIEW))
                .build();

        // add the shortcuts
        if (!ShortcutManagerCompat.addDynamicShortcuts(this, listOf(scSearch, scProfile, scLibrary, scHome)))
            Log.w("Tenshi", "failed to add static app shortcuts!");
    }

    /**
     * cleanup the database
     */
    public void cleanupDatabase() {
        async(() -> {
            final int removedEntities = database.cleanupDatabase();
            Log.i("Tenshi", fmt("Database cleanup finished with %d entities removed", removedEntities));
        });
    }

    /**
     * deletes user auth data, database and all that stuff for logout
     * <p>
     * Deletes:
     * <li>AuthToken (prefs and {@link #token}</li>
     * <li>UserID (prefs)</li>
     * <li>Database (all tables)</li>
     */
    public void deleteUserData() {
        // clear auth token
        TenshiPrefs.clear(TenshiPrefs.Key.AuthToken);
        token = null;

        // clear saved user id
        TenshiPrefs.clear(TenshiPrefs.Key.UserID);

        // clear database (my_list_status is invalid after user switch)
        async(() -> database.clearAllTables());
    }

    // region AUTH

    /**
     * try to load the auth token from prefs and initialize the MAL service
     */
    public void tryAuthInit() {
        token = TenshiPrefs.getObject(TenshiPrefs.Key.AuthToken, Token.class, null);
        if (notNull(token))
            createRetrofit();
    }

    /**
     * set the applications auth token, save it to prefs and initialize the MAL service
     *
     * @param t the new token to set
     */
    public void setTokenAndTryAuthInit(@Nullable Token t) {
        token = t;
        if (notNull(token)) {
            TenshiPrefs.setObject(TenshiPrefs.Key.AuthToken, token);
            createRetrofit();
        }
    }

    /**
     * invalidate and remove the saved auth token, then redirect to the Login activity
     * you could also call this "logout"
     *
     * @param ctx the context to start the login activity from. has to be another activity, on which .finish() is called
     */
    public void invalidateTokenAndLogin(@NonNull Activity ctx) {
        // delete the user's data
        deleteUserData();

        // show toast
        Toast.makeText(ctx, R.string.login_toast_session_expired, Toast.LENGTH_SHORT).show();

        // go to login activity
        Intent i = new Intent(ctx, OnboardingActivity.class);
        ctx.startActivity(i);
        ctx.finish();
    }

    /**
     * try to refresh the user auth token
     *
     * @param ctx the activity context to work with. has to be activity because calls to invalidateTokenAndLogin() are made on errors, which opens another activity
     */
    public void tryRefreshAuth(@NonNull final Activity ctx) {
        // only allow calling once
        if (currentlyRefreshingToken)
            return;
        currentlyRefreshingToken = true;

        // ensure we have a refresh token
        if (isNull(token) || isNull(token.refreshToken))
            return;

        // do refresh call
        MalApiHelper.doRefreshToken(token.refreshToken, new Callback<Token>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {
                    // get new token
                    token = response.body();

                    // if token is null, refresh failed
                    // else everything is ok and we can recreate the retrofit services
                    if (isNull(token) || nullOrEmpty(token.token))
                        invalidateTokenAndLogin(ctx);
                    else
                        setTokenAndTryAuthInit(token);
                } else {
                    // refresh failed
                    invalidateTokenAndLogin(ctx);
                }

                currentlyRefreshingToken = false;
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Token> call, Throwable t) {
                Log.e("Tenshi", t.toString());
                invalidateTokenAndLogin(ctx);
                currentlyRefreshingToken = false;
            }
        });
    }
    //endregion

    // region Init API

    /**
     * create the MAL service retrofit instance.
     * only valid if isUserAuthenticated is true
     */
    private void createRetrofit() {
        // init okhttp client
        final OkHttpClient client = createOkHttpClient();
        if (isNull(client))
            return;

        // init retrofit
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Urls.API)
                .addConverterFactory(new RetrofitEnumConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .client(client)
                .build();

        // create service
        mal = retrofit.create(MalService.class);
    }

    /**
     * create a OKHttp client with the required interceptors (for auth, ...)
     * On DEBUG builds, a logging interceptor is added aswell
     *
     * @return the OKHttp client instance
     */
    private OkHttpClient createOkHttpClient() {
        // get token and token type
        final String tokenType = elvisEmpty(token.type, "Bearer");
        final String token = this.token.token;
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
        return new Cache(getCacheDir(), cacheSize);
    }
    //endregion

    //region Static Access

    /**
     * global callback for all responses we get from the MAL api (MalService).
     * Used for automatic re- auth
     *
     * @param ctx      the activity context the call of the response happened in
     * @param response the response
     */
    public static void malReauthCallback(@NonNull Activity ctx, @NonNull Response<?> response) {
        // this is a global callback, so be careful what you do here ;)
        // check if this response failed with 401
        if (response.code() == 401)
            INSTANCE.tryRefreshAuth(ctx);
    }

    /**
     * @return the global GSON instance, with additional adapters
     */
    @NonNull
    public static Gson getGson() {
        if (isNull(INSTANCE.gson)) {
            INSTANCE.gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new GSONLocalDateAdapter().nullSafe())
                    .registerTypeAdapter(LocalTime.class, new GSONLocalTimeAdapter().nullSafe())
                    .registerTypeAdapter(ZonedDateTime.class, new GSONZonedDateTimeAdapter().nullSafe())
                    .create();
        }

        return INSTANCE.gson;
    }

    /**
     * @return the global MAL api service
     */
    @NonNull
    public static MalService getMal() {
        return INSTANCE.mal;
    }

    /**
     * @return the content adapter manager instance
     */
    @NonNull
    public static ContentAdapterManager getContentAdapterManager() {
        return INSTANCE.contentAdapterManager;
    }

    /**
     * @return the offline database instance
     */
    @NonNull
    public static TenshiDB getDB() {
        return INSTANCE.database;
    }

    /**
     * @return is a user authenticated and we have a access token?
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isUserAuthenticated() {
        return notNull(INSTANCE.token) && !nullOrWhitespace(INSTANCE.token.token);
    }
    //endregion
}
