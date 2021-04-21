package io.github.shadow578.tenshi.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.transition.platform.MaterialFadeThrough;

import java.util.List;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.databinding.ActivityOnboardingBinding;
import io.github.shadow578.tenshi.mal.MalApiHelper;
import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.User;
import io.github.shadow578.tenshi.mal.model.UserLibraryEntry;
import io.github.shadow578.tenshi.mal.model.UserLibraryList;
import io.github.shadow578.tenshi.mal.model.type.LibrarySortMode;
import io.github.shadow578.tenshi.ui.MainActivity;
import io.github.shadow578.tenshi.ui.TenshiActivity;
import io.github.shadow578.tenshi.util.TenshiPrefs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.async;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrWhitespace;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;

/**
 * activity that handles initial onboarding, including:
 * - login
 * - initial configuration
 * - data preloading (user library and profile)
 */
public class OnboardingActivity extends TenshiActivity {
    /**
     * extra to only handle login, skipping the rest of onboarding
     * boolean
     */
    public static String EXTRA_ONLY_LOGIN = "onlyLogin";

    /**
     * the current onboarding state
     */
    private State state;

    private enum State {
        /**
         * login to MAL.
         * after login is finished, start fetching data in the background
         */
        Login,

        /**
         * let user select the app theme
         */
        Theme,

        /**
         * let user set NSFW preference
         */
        NSFW,

        /**
         * Onboarding finished. maybe wait until background fetch finished, then open main activity
         */
        Finished
    }

    /**
     * did initial fetching finish?
     */
    private boolean fetchFinished = false;

    /**
     * should onboarding be skipped?
     */
    private boolean onlyLogin = false;

    private final LoginFragment loginFragment = new LoginFragment();
    private final ThemeSelectFragment themeSelectFragment = new ThemeSelectFragment();
    private final NSFWSelectFragment nsfwSelectFragment = new NSFWSelectFragment();
    private ActivityOnboardingBinding b;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // get intent extras
        with(getIntent(), i -> onlyLogin = i.getBooleanExtra(EXTRA_ONLY_LOGIN, false));

        // setup fragments
        setFragmentTransitions();
        setFragmentEvents();

        // go to login activity
        updateState(State.Login);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // forward to login fragment if in login state
        if (state == State.Login)
            loginFragment.onNewIntent(intent);
    }

    /**
     * update the onboarding state and update the fragment
     *
     * @param newState the state to switch to
     */
    private void updateState(State newState) {
        // update state
        state = newState;

        // get target fragment
        Fragment target;
        switch (state) {
            default:
            case Login:
                target = loginFragment;
                break;
            case Theme:
                target = themeSelectFragment;
                break;
            case NSFW:
                target = nsfwSelectFragment;
                break;
            case Finished:
                maybeContinueToMain();
                return;
        }

        // switch to the fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_fragment_container, target)
                .commit();
    }

    /**
     * initialize transitions for all framgents
     */
    private void setFragmentTransitions() {
        // prepare common transition for all fragments
        final MaterialFadeThrough f = new MaterialFadeThrough();

        // login
        loginFragment.setEnterTransition(f);
        loginFragment.setExitTransition(f);

        // theme
        themeSelectFragment.setEnterTransition(f);
        themeSelectFragment.setExitTransition(f);

        // nsfw
        nsfwSelectFragment.setEnterTransition(f);
        nsfwSelectFragment.setExitTransition(f);
    }

    /**
     * initialize events for all fragments
     */
    private void setFragmentEvents() {
        // login
        loginFragment.setLoginListener(success -> {
            if (success) {
                // initialize api, if failed redirect to login (should not happen...)
                TenshiApp.INSTANCE.tryAuthInit();
                requireUserAuthenticated();

                // TODO: as onboarding fragments are not yet in place, onlyLogin is overwritten for now
                onlyLogin = true;

                if (onlyLogin) {
                    // done, finish now
                    fetchFinished = true;
                    updateState(State.Finished);
                } else {
                    // start fetching data
                    fetchUserData();

                    // continue to theme selection fragment
                    updateState(State.Theme);
                }
            }
            // if not success, fragment has already shown error cause in a snackbar, so we don't do anything
        });

    }

    /**
     * fetch the user's data into the local DB
     * - profile
     * - library
     */
    private void fetchUserData() {
        fetchFinished = false;

        // fetch profile; swallow errors
        final String USER_FIELDS = MalApiHelper.getQueryableFields(User.class);
        TenshiApp.getMal().getCurrentUser(USER_FIELDS)
                .enqueue(new Callback<User>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && notNull(response.body())) {
                            // insert into db
                            User user = response.body();
                            async(() -> TenshiApp.getDB().userDB().insertOrUpdateUser(user));

                            // save user ID to prefs
                            TenshiPrefs.setInt(TenshiPrefs.Key.UserID, user.userID);
                        }
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("Tenshi", t.toString());
                    }
                });

        // prepare callback for anime details fetching
        final String ANIME_FIELDS = MalApiHelper.getQueryableFields(Anime.class);
        final Callback<Anime> animeDetailsCallback = new Callback<Anime>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Anime> c, Response<Anime> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // insert into db
                    Anime anime = response.body();
                    async(() -> TenshiApp.getDB().animeDB().insertAnime(anime));
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Anime> c, Throwable t) {
                Log.e("Tenshi", t.toString());
            }
        };

        // fetch library, all statuses; swallow errors
        final String LIB_FIELDS = "main_picture,title,list_status{score},num_episodes,status,nsfw";
        TenshiApp.getMal().getCurrentUserLibrary(null, LibrarySortMode.anime_id, LIB_FIELDS, 1)
                .enqueue(new Callback<UserLibraryList>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<UserLibraryList> call, Response<UserLibraryList> response) {
                        if (response.isSuccessful()) {
                            // get response
                            UserLibraryList library = response.body();
                            if (notNull(library)) {
                                // insert anime into the db
                                List<UserLibraryEntry> libraryList = library.items;
                                async(() -> TenshiApp.getDB().animeDB().insertLibraryAnime(libraryList));

                                // fetch all anime details
                                for (UserLibraryEntry e : libraryList)
                                    TenshiApp.getMal().getAnime(e.anime.animeId, ANIME_FIELDS).enqueue(animeDetailsCallback);

                                // load next page
                                if (notNull(library.paging) && !nullOrWhitespace(library.paging.nextPage))
                                    TenshiApp.getMal().getUserAnimeListPage(library.paging.nextPage).enqueue(this);
                                else {
                                    fetchFinished = true;
                                    async(() -> null, p -> maybeContinueToMain());
                                }
                            }
                        }
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<UserLibraryList> call, Throwable t) {
                        Log.e("Tenshi", t.toString());
                    }
                });
    }

    /**
     * continue to the main activity if state is correct
     */
    private void maybeContinueToMain() {
        if (state == State.Finished && fetchFinished) {
            Intent i = new Intent(this, MainActivity.class);
            startActivityForResult(i, MainActivity.REQUEST_LOGIN);
        }
    }
}
