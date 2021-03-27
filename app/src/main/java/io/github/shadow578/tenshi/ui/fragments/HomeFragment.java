package io.github.shadow578.tenshi.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.adapter.AnimeListAdapter;
import io.github.shadow578.tenshi.adapter.SeasonalAnimeAdapter;
import io.github.shadow578.tenshi.databinding.FragmentHomeBinding;
import io.github.shadow578.tenshi.mal.MalApiHelper;
import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.AnimeList;
import io.github.shadow578.tenshi.mal.model.AnimeListItem;
import io.github.shadow578.tenshi.mal.model.AnimeListRanking;
import io.github.shadow578.tenshi.mal.model.AnimeRankingItem;
import io.github.shadow578.tenshi.mal.model.Season;
import io.github.shadow578.tenshi.mal.model.type.RankingType;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;
import io.github.shadow578.tenshi.util.DateHelper;
import io.github.shadow578.tenshi.util.LocalizationHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.*;


/**
 * home fragment, showing recommendations and seasonal anime to the user
 */
public class HomeFragment extends TenshiFragment {

    private static final String REQUEST_FIELDS = MalApiHelper.getQueryableFields(Anime.class);

    private final ArrayList<AnimeRankingItem> animeListSeasonal = new ArrayList<>();
    private final ArrayList<AnimeListItem> animeListRecommend = new ArrayList<>();
    private final Season currentSeason = DateHelper.getSeason();
    private FragmentHomeBinding b;
    private SeasonalAnimeAdapter seasonalAnimeAdapter;
    private AnimeListAdapter recommendedAnimeAdapter;
    private AnimeListRanking seasonalAnimeResponse = null;
    private AnimeList recommendedAnimeResponse = null;
    private int showNSFW = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load nsfw preference
        showNSFW = TenshiPrefs.getBool(TenshiPrefs.Key.NSFW, false) ? 1 : 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentHomeBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // hide/show loading indicator for seasonal anime
        if (animeListSeasonal.isEmpty())
            b.currentSeasonLoadingIndicator.show();
        else
            b.currentSeasonLoadingIndicator.hide();

        // init adapter for seasonal anime
        seasonalAnimeAdapter = new SeasonalAnimeAdapter(
                requireContext(),
                animeListSeasonal,
                (v, anime) -> openDetails(anime.anime.animeId, v));
        b.currentSeasonRecycler.setAdapter(seasonalAnimeAdapter);

        // update title for seasonal anime list
        b.currentSeasonTitle.setText(concat(LocalizationHelper.localizeSeason(currentSeason.season, requireContext()), " ", str(currentSeason.year)));

        // hide/show loading indicator for recommended anime
        b.recommendationsLoadingIndicator.show();
        if (!animeListRecommend.isEmpty())
            b.recommendationsLoadingIndicator.hide();

        // init adapter for recommended anime
        recommendedAnimeAdapter = new AnimeListAdapter(
                requireContext(),
                animeListRecommend,
                (v, anime) -> openDetails(anime.anime.animeId, v));
        recommendedAnimeAdapter.setEndListener(position -> {
            if (recommendedAnimeResponse != null && recommendedAnimeResponse.paging != null) {
                String nextPageUrl = recommendedAnimeResponse.paging.nextPage;
                if (!nullOrEmpty(nextPageUrl))
                    fetchRecommendedAnime(TenshiApp.getMal().getAnimeList(nextPageUrl), false);
            }
        });
        b.recommendationsRecycler.setAdapter(recommendedAnimeAdapter);

        // fetch data from MAL
        fetchAnime();
    }

    /**
     * initially fetch recommended and seasonal anime
     */
    private void fetchAnime() {
        // get popular anime
        fetchSeasonalAnime(true);

        // get recommended anime
        Call<AnimeList> recommendCall = TenshiApp.getMal().getAnimeRecommendations(30, REQUEST_FIELDS);
        fetchRecommendedAnime(recommendCall, true);
    }

    /**
     * fetch seasonal anime, add to animeListSeasonal automatically
     *
     * @param shouldClear should the current list be cleared?
     */
    private void fetchSeasonalAnime(@SuppressWarnings("SameParameterValue") boolean shouldClear) {
        TenshiApp.getMal().getAnimeRanking(RankingType.Airing, "start_season", 200, showNSFW)
                .enqueue(new Callback<AnimeListRanking>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<AnimeListRanking> call, Response<AnimeListRanking> response) {
                        // callback to TenshiApp global reauth
                        if (isAdded())
                            TenshiApp.malReauthCallback(requireActivity(), response);

                        if (response.isSuccessful()) {
                            // get response
                            seasonalAnimeResponse = response.body();
                            if (notNull(seasonalAnimeResponse)) {

                                // get anime from response
                                List<AnimeRankingItem> newAnime = seasonalAnimeResponse.items;
                                if (shouldClear)
                                    animeListSeasonal.clear();


                                // only add anime in the current season
                                for (AnimeRankingItem a : newAnime)
                                    if (a.anime.startSeason != null
                                            && a.anime.startSeason.equals(currentSeason))
                                        animeListSeasonal.add(a);

                                // update ui
                                seasonalAnimeAdapter.notifyDataSetChanged();
                                b.currentSeasonLoadingIndicator.hide();
                            }
                        } else if (response.code() == 401 && isAdded())
                            Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<AnimeListRanking> call, Throwable t) {
                        Log.e("Tenshi", t.toString());
                        if (!animeListSeasonal.isEmpty())
                            b.currentSeasonLoadingIndicator.hide();
                        if (isAdded())
                            Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * fetch recommended anime, add to animeListRecommend automatically
     *
     * @param call        the call to execute
     * @param shouldClear should the current list be cleared?
     */
    private void fetchRecommendedAnime(@NonNull Call<AnimeList> call, boolean shouldClear) {
        call.enqueue(new Callback<AnimeList>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<AnimeList> call, Response<AnimeList> response) {
                // callback to TenshiApp global reauth
                if (isAdded())
                    TenshiApp.malReauthCallback(requireActivity(), response);

                if (response.isSuccessful()) {
                    // get response body
                    recommendedAnimeResponse = response.body();
                    if (notNull(recommendedAnimeResponse)) {
                        // clear if requested
                        if (shouldClear)
                            animeListRecommend.clear();

                        // add all anime to list
                        animeListRecommend.addAll(recommendedAnimeResponse.items);

                        // update ui
                        recommendedAnimeAdapter.notifyDataSetChanged();
                        b.recommendationsLoadingIndicator.hide();
                    }
                } else if (response.code() == 401 && isAdded())
                    Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();

            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<AnimeList> call, Throwable t) {
                Log.e("Tenshi", t.toString());
                if (!animeListRecommend.isEmpty())
                    b.recommendationsLoadingIndicator.hide();
                if (isAdded())
                    Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * open the details of a anime
     *
     * @param animeId the anime to open
     * @param view    the view to use for the transition
     */
    private void openDetails(int animeId, @Nullable View view) {
        Bundle bundle = null;
        if (view != null) {
            ImageView poster = view.findViewById(R.id.anime_main_poster);
            ActivityOptionsCompat opt = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), poster, poster.getTransitionName());
            bundle = opt.toBundle();
        }

        Intent i = new Intent(getContext(), AnimeDetailsActivity.class);
        i.putExtra(AnimeDetailsActivity.EXTRA_ANIME_ID, animeId);
        startActivity(i, bundle);
    }
}
