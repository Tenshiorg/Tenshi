package io.github.shadow578.tenshi;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import io.github.shadow578.tenshi.mal.AuthInterceptor;
import io.github.shadow578.tenshi.mal.CacheInterceptor;
import io.github.shadow578.tenshi.mal.MALErrorInterceptor;
import io.github.shadow578.tenshi.mal.MalApiHelper;
import io.github.shadow578.tenshi.mal.MalService;
import io.github.shadow578.tenshi.mal.Urls;
import io.github.shadow578.tenshi.mal.model.Token;
import io.github.shadow578.tenshi.ui.LoginActivity;
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

import static io.github.shadow578.tenshi.lang.LanguageUtils.elvisEmpty;
import static io.github.shadow578.tenshi.lang.LanguageUtils.fmt;
import static io.github.shadow578.tenshi.lang.LanguageUtils.isNull;
import static io.github.shadow578.tenshi.lang.LanguageUtils.notNull;
import static io.github.shadow578.tenshi.lang.LanguageUtils.nullOrEmpty;
import static io.github.shadow578.tenshi.lang.LanguageUtils.nullOrWhitespace;

/**
 * Tenshi Core logic
 */
public class TenshiApp extends Application {
    /**
     * Singleton instance
     */
    public static TenshiApp INSTANCE;

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

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        TenshiPrefs.init(getApplicationContext());
        tryAuthInit();
    }

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
     * invalidate and remove the saved auth token.
     * you could also call this "logout"
     */
    public void invalidateToken() {
        TenshiPrefs.clear(TenshiPrefs.Key.AuthToken);
        token = null;
    }

    /**
     * invalidate and remove the saved auth token, then redirect to the Login activity
     * you could also call this "logout"
     *
     * @param ctx the context to start the login activity from. has to be another activity, on which .finish() is called
     */
    public void invalidateTokenAndLogin(@NonNull Activity ctx) {
        // invalidate token
        invalidateToken();

        // show toast
        Toast.makeText(ctx, R.string.login_toast_session_expired, Toast.LENGTH_SHORT).show();

        // go to login activity
        Intent i = new Intent(ctx, LoginActivity.class);
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
                .addConverterFactory(GsonConverterFactory.create(createGson()))
                .client(client)
                .build();

        // create service
        mal = retrofit.create(MalService.class);
    }

    /**
     * create a Gson instance with required adapters for MAL api
     *
     * @return the gson instance
     */
    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new GSONLocalDateAdapter().nullSafe())
                .registerTypeAdapter(LocalTime.class, new GSONLocalTimeAdapter().nullSafe())
                .registerTypeAdapter(ZonedDateTime.class, new GSONZonedDateTimeAdapter().nullSafe())
                .create();
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
     * @return the global MAL api service
     */
    @NonNull
    public static MalService getMal() {
        return INSTANCE.mal;
    }

    /**
     * @return is a user authenticated and we have a access token?
     */
    public static boolean isUserAuthenticated() {
        return notNull(INSTANCE.token) && !nullOrWhitespace(INSTANCE.token.token);
    }
    //endregion
}
