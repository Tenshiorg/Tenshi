package io.github.shadow578.tenshi.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.adapter.AnimeListAdapter;
import io.github.shadow578.tenshi.databinding.ActivitySearchBinding;
import io.github.shadow578.tenshi.mal.MalApiHelper;
import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.AnimeList;
import io.github.shadow578.tenshi.mal.model.AnimeListItem;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;
import io.github.shadow578.tenshi.ui.TenshiActivity;
import io.github.shadow578.tenshi.util.TenshiPrefs;
import io.github.shadow578.tenshi.util.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.async;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;

/**
 * anime search activity (search by text / MAL search)
 */
public class SearchActivity extends TenshiActivity {

    private static final String REQUEST_FIELDS = MalApiHelper.getQueryableFields(Anime.class);

    private ActivitySearchBinding b;
    private AnimeListAdapter searchAdapter;
    private final ArrayList<AnimeListItem> searchResults = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // check user is logged in, redirect to login and finish() if not
        requireUserAuthenticated();

        // notify user if offline
        showSnackbarIfOffline(b.getRoot());

        // setup status & actionbar
        setSupportActionBar(b.searchToolbar);
        with(getSupportActionBar(), sab -> {
            sab.setDisplayHomeAsUpEnabled(true);
            sab.setHomeButtonEnabled(true);
            sab.setDisplayShowTitleEnabled(false);
        });
        b.searchToolbar.setNavigationOnClickListener(v -> onBackPressed());

        // hide loading indicator
        b.searchLoadingIndicator.hide();

        // setup recycler view for search results
        searchAdapter = new AnimeListAdapter(
                this,
                searchResults,
                (v, anime) -> openDetails(anime.anime.animeId, v));
        b.searchResultsRecycler.setAdapter(searchAdapter);

        // setup search view
        final SearchView searchView = b.searchToolbar.findViewById(R.id.search_view);
        searchView.setQueryHint(getString(R.string.search_query_hint));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!nullOrEmpty(query))
                    searchAnime(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    /**
     * perform a search using the given query string.
     * The results are written directly into searchResults, and the recycler is notified
     *
     * @param query the search query
     */
    private void searchAnime(@NonNull String query) {
        // clear previous results & show loading indicator
        searchResults.clear();
        b.searchLoadingIndicator.show();

        // if we are offline search using the db
        if (Util.getConnectionType(this).equals(Util.ConnectionType.None))
            async(() -> TenshiApp.getDB().animeDB().searchAnime(query), results -> {
                // only use search results from db if we dont already have results from MAL
                // ... somehow
                if (searchResults.isEmpty() && notNull(results)) {
                    searchResults.addAll(results);
                    searchAdapter.notifyDataSetChanged();
                    b.noResultText.setVisibility(View.GONE);
                    b.searchLoadingIndicator.hide();
                }
            });

        // search MAL
        final int showNSFW = TenshiPrefs.getBool(TenshiPrefs.Key.NSFW, false) ? 1 : 0;
        TenshiApp.getMal().searchAnime(query, null, showNSFW, REQUEST_FIELDS)
                .enqueue(new Callback<AnimeList>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<AnimeList> call, Response<AnimeList> response) {
                        // callback to TenshiApp global reauth
                        TenshiApp.malReauthCallback(SearchActivity.this, response);

                        if (response.isSuccessful()) {
                            // clear previous results (from db) & hide loading indicator
                            searchResults.clear();
                            b.searchLoadingIndicator.hide();

                            AnimeList result = response.body();
                            if (notNull(result) && !nullOrEmpty(result.items)) {
                                // we have search results, add them to the list
                                searchResults.addAll(result.items);
                                b.searchResultsRecycler.setVisibility(View.VISIBLE);
                                b.noResultText.setVisibility(View.GONE);
                            } else {
                                // no search results, show "no results" text and hide recycler
                                b.searchResultsRecycler.setVisibility(View.GONE);
                                b.noResultText.setVisibility(View.VISIBLE);
                            }

                            // notify recycler the data changed
                            searchAdapter.notifyDataSetChanged();
                        } else if (response.code() == 401)
                            Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<AnimeList> call, Throwable t) {
                        Log.e("Tenshi", t.toString());
                        b.searchLoadingIndicator.hide();
                        Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * open the details for a anime
     *
     * @param animeId the id of the anime to open the details of
     * @param v       the view to use for the transition
     */
    private void openDetails(int animeId, @NonNull View v) {
        final ImageView poster = v.findViewById(R.id.anime_main_poster);
        final ActivityOptionsCompat opt = ActivityOptionsCompat.makeSceneTransitionAnimation(this, poster, poster.getTransitionName());
        final Intent i = new Intent(this, AnimeDetailsActivity.class);
        i.putExtra(AnimeDetailsActivity.EXTRA_ANIME_ID, animeId);
        startActivity(i, opt.toBundle());
    }
}
