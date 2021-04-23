package io.github.shadow578.tenshi.ui.oobe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
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
 * - MAL api check
 * - login
 * - initial configuration
 * - data preloading (user library and profile)
 */
public class OnboardingActivity extends TenshiActivity {
    /**
     * extra to only handle login, skipping the rest of onboarding; boolean
     * <p>
     * TODO skip full onboarding when already logged in / just require reauth
     */
    public static final String EXTRA_ONLY_LOGIN = "onlyLogin";

    /**
     * the current onboarding state
     */
    private State state;

    private enum State {
        /**
         * connection check (device online, MAL api reachable)
         */
        ConnectionCheck,

        /**
         * login to MAL.
         * after login is finished, start fetching data in the background
         */
        Login,

        /**
         * initial configuration, material self- select style
         */
        Configuration,

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

    private final OnlineCheckFragment onlineCheckFragment = new OnlineCheckFragment();
    private final LoginFragment loginFragment = new LoginFragment();
    private final InitialConfigurationFragment configurationFragment = new InitialConfigurationFragment();
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

        // hide loading for final state
        setFinishingStateLoadingVisible(false);

        // go to online check
        updateState(State.ConnectionCheck);
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
            case ConnectionCheck:
                target = onlineCheckFragment;
                break;
            case Login:
                target = loginFragment;
                break;
            case Configuration:
                target = configurationFragment;
                break;
            case Finished:
                setFinishingStateLoadingVisible(true);
                maybeContinueToMain();
                return;
        }

        // switch to the fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, target)
                .commit();
    }

    /**
     * initialize transitions for all framgents
     */
    private void setFragmentTransitions() {
        // prepare common transition for all fragments
        final MaterialFadeThrough f = new MaterialFadeThrough();

        // connection check
        onlineCheckFragment.setEnterTransition(f);
        onlineCheckFragment.setExitTransition(f);

        // login
        loginFragment.setEnterTransition(f);
        loginFragment.setExitTransition(f);

        // config
        configurationFragment.setEnterTransition(f);
        configurationFragment.setExitTransition(f);
    }

    /**
     * initialize events for all fragments
     */
    private void setFragmentEvents() {
        // online check
        onlineCheckFragment.setOnSuccessListener(() -> {
            // continue to login
            updateState(State.Login);
        });

        // login
        // on fail, we don't have to do anything more, as the fragment already shows error info
        loginFragment.setOnSuccessListener(() -> {
            // initialize api, if failed redirect to login (should not happen...)
            TenshiApp.INSTANCE.tryAuthInit();
            requireUserAuthenticated();

            if (onlyLogin) {
                // done, finish now
                fetchFinished = true;
                updateState(State.Finished);
            } else {
                // start fetching data
                // this calls afterUserProfileFetched() after the user profile was fetched,
                // which then updates and shows the configuration fragment
                fetchUserData();
            }
        });

        // config
        configurationFragment.setOnSuccessListener(() -> {
            // config is done, continue to main once library fetch finished
            updateState(State.Finished);
        });
    }

    /**
     * called after the user profile finished fetching in {@link #fetchUserData()}
     *
     * @param user the user data fetched
     */
    private void afterUserProfileFetched(@NonNull User user) {
        // continue to configuration selection fragment
        configurationFragment.setUserDetails(user);
        updateState(State.Configuration);
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

                            // continue ui
                            afterUserProfileFetched(user);
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
        TenshiApp.getMal().getCurrentUserLibrary(null, LibrarySortMode.anime_title, LIB_FIELDS, 1)
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

    /**
     * sets the visibility of all views related to the "Finished" state.
     * This includes a loading indicator with text that is shown when user data is fetched in the background, but the user is already finished with all previous steps
     *
     * @param v currently in finished state?
     */
    private void setFinishingStateLoadingVisible(boolean v) {
        // show or hide loading indicator + label
        b.loadingIndicator.setVisibility(v ? View.VISIBLE : View.GONE);
        b.loadingIndicatorLabel.setVisibility(v ? View.VISIBLE : View.GONE);

        // hide or show the fragment container
        b.fragmentContainer.setVisibility(v ? View.INVISIBLE : View.VISIBLE);
    }
}
