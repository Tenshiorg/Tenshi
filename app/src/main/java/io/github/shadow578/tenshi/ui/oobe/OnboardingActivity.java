package io.github.shadow578.tenshi.ui.oobe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.transition.platform.MaterialFadeThrough;

import java.util.List;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.databinding.ActivityOnboardingBinding;
import io.github.shadow578.tenshi.mal.MalApiHelper;
import io.github.shadow578.tenshi.mal.model.Anime;
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

/**
 * activity that handles initial onboarding, including:
 * - MAL api check
 * - login
 * - initial configuration
 * - data preloading (user library and profile)
 */
public class OnboardingActivity extends TenshiActivity {
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

    private final OnlineCheckFragment onlineCheckFragment = new OnlineCheckFragment();
    private final LoginFragment loginFragment = new LoginFragment();
    private final InitialConfigurationFragment configurationFragment = new InitialConfigurationFragment();
    private final FetchFinishFragment fetchFinishFragment = new FetchFinishFragment();
    private ActivityOnboardingBinding b;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // setup fragments
        setupFragmentVisuals();
        setupFragmentEvents();

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
        OnboardingFragment target;
        switch (state) {
            default:
            case ConnectionCheck:
                updateButtons(null, null);
                target = onlineCheckFragment;
                break;
            case Login:
                updateButtons(null, null);
                target = loginFragment;
                break;
            case Configuration:
                updateButtons(State.Login, State.Finished);
                target = configurationFragment;
                break;
            case Finished:
                updateButtons(null, null);
                target = fetchFinishFragment;
                if (maybeContinueToMain())
                    return;
                break;
        }

        // switch to the fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, target)
                .commit();

        // select dot (tab)
        final TabLayout.Tab tab = target.getTab();
        if (notNull(tab))
            b.fragmentDots.selectTab(tab);
    }

    /**
     * initialize visuals for all fragments
     */
    private void setupFragmentVisuals() {
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

        // fetch finish
        fetchFinishFragment.setEnterTransition(f);
        fetchFinishFragment.setExitTransition(f);

        // create tabs (dots) for all fragments
        onlineCheckFragment.initTab(b.fragmentDots);
        loginFragment.initTab(b.fragmentDots);
        configurationFragment.initTab(b.fragmentDots);
        //fetchFinishFragment.initTab(b.fragmentDots);
    }

    /**
     * initialize events for all fragments
     */
    private void setupFragmentEvents() {
        // online check
        onlineCheckFragment.setOnSuccessListener(() -> {
            // continue to login
            updateState(State.Login);
        });

        // login
        loginFragment.setOnSuccessListener(() -> {
            // initialize api, if failed redirect to login (should not happen...)
            TenshiApp.INSTANCE.tryAuthInit();
            requireUserAuthenticated();

            // don't show initial setup and fetch again if OOBE was already finished before
            if (TenshiPrefs.getBool(TenshiPrefs.Key.OOBEFinished, false)) {
                fetchFinished = true;
                updateState(State.Finished);
            } else {
                startFetchUserLibrary();
                updateButtons(null, State.Configuration);
            }
        });
    }

    /**
     * fetch the user's library into the local DB
     */
    private void startFetchUserLibrary() {
        fetchFinished = false;

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
     *
     * @return did we continue to main?
     */
    private boolean maybeContinueToMain() {
        if (state == State.Finished && fetchFinished) {
            TenshiPrefs.setBool(TenshiPrefs.Key.OOBEFinished, true);
            Intent i = new Intent(this, MainActivity.class);
            startActivityForResult(i, MainActivity.REQUEST_LOGIN);
            return true;
        }

        return false;
    }

    /**
     * update the back/next buttons to update to the given states.
     * if a state is null, the button is disabled
     *
     * @param prev the state for the back button
     * @param next the state for the next button
     */
    private void updateButtons(@Nullable State prev, @Nullable State next) {
        if (notNull(prev)) {
            b.backBtn.setVisibility(View.VISIBLE);
            b.backBtn.setOnClickListener(v -> updateState(prev));
        } else
            b.backBtn.setVisibility(View.INVISIBLE);

        if (notNull(next)) {
            b.nextBtn.setVisibility(View.VISIBLE);
            b.nextBtn.setOnClickListener(v -> updateState(next));
        } else
            b.nextBtn.setVisibility(View.INVISIBLE);
    }
}
