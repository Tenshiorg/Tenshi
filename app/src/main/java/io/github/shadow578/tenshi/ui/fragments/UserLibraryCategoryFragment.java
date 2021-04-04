package io.github.shadow578.tenshi.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.adapter.UserAnimeListAdapter;
import io.github.shadow578.tenshi.databinding.FragmentAnimelistCategoryBinding;
import io.github.shadow578.tenshi.extensionslib.lang.BiConsumer;
import io.github.shadow578.tenshi.mal.model.UserLibraryEntry;
import io.github.shadow578.tenshi.mal.model.UserLibraryList;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.mal.model.type.LibrarySortMode;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;
import io.github.shadow578.tenshi.util.TenshiPrefs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.async;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;

/**
 * fragment for viewing a specific category of the user library
 */
public class UserLibraryCategoryFragment extends TenshiFragment {
    /**
     * request code for opening anime details
     */
    public static final int REQUEST_ANIME_DETAILS = 17;

    private static final String REQUEST_FIELDS = "main_picture,title,list_status{score},num_episodes,status,nsfw";//MalApiHelper.getQueryableFields(UserLibraryEntry.class);

    private UserLibraryList animeListResponse;
    private UserAnimeListAdapter animeListAdapter;
    private final List<UserLibraryEntry> animeList = new ArrayList<>();
    private final LibraryEntryStatus listStatus;
    private LibrarySortMode sortMode;
    private int showNSFW;

    private FragmentAnimelistCategoryBinding b;

    @Nullable
    private BiConsumer<Integer, Integer> scrollListener;

    public UserLibraryCategoryFragment(@NonNull LibraryEntryStatus status, @NonNull LibrarySortMode initialSort) {
        listStatus = status;
        sortMode = initialSort;
    }

    /**
     * update the sort mode of the visible anime
     *
     * @param mode the mode to use
     */
    public void setSortMode(@NonNull LibrarySortMode mode) {
        if (sortMode != mode) {
            // only if the mode changed
            sortMode = mode;

            // clear currently loaded list and re- fetch anime
            animeList.clear();
            fetchAnimeList();
        }
    }

    /**
     * set the scroll listener
     *
     * @param l the listener to set
     */
    public void setOnScrollChangeListener(@Nullable BiConsumer<Integer /*deltaX*/, Integer /*deltaY*/> l) {
        scrollListener = l;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get preferences
        showNSFW = TenshiPrefs.getBool(TenshiPrefs.Key.NSFW, false) ? 1 : 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentAnimelistCategoryBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // hide loading indicator if we loaded anime
        if (animeList.isEmpty())
            b.animeListLoadingIndicator.show();
        else
            b.animeListLoadingIndicator.hide();

        // init adapter, call openDetails on click
        animeListAdapter = new UserAnimeListAdapter(requireContext(), animeList, (itmView, anime)
                -> openDetails(anime.anime.animeId, itmView));

        // setup adapter so we load the next page before reaching the end of the current page
        animeListAdapter.setEndListener(position -> {
            if (animeListResponse != null && animeListResponse.paging != null) {
                String nextPage = animeListResponse.paging.nextPage;
                if (!nullOrEmpty(nextPage)) {
                    Call<UserLibraryList> nextPageCall = TenshiApp.getMal().getUserAnimeListPage(nextPage);
                    fetchAnimeList(nextPageCall, false);
                }
            }
        });

        // set adapter on recycler view
        b.animeListRecycler.setAdapter(animeListAdapter);

        // add scroll change listener
        b.animeListRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (notNull(scrollListener))
                    scrollListener.invoke(dx, dy);
            }
        });

        // fetch initial data from MAL
        fetchAnimeList();
    }

    /**
     * prepare the call to fetchAnime()
     */
    private void fetchAnimeList() {
        // show loading indicator
        b.animeListLoadingIndicator.show();

        // load from db
        async(() -> TenshiApp.getDB().animeDB().getUserLibrary(listStatus, sortMode, showNSFW == 1), lib -> {
            // only load if not already loaded from MAL but found
            if(animeList.isEmpty() && notNull(lib)){
                // update data
                animeList.addAll(lib);
                animeListAdapter.notifyDataSetChanged();

                // hide loading indicator
                b.animeListLoadingIndicator.hide();
            }
        });

        // get call to load anime list
        Call<UserLibraryList> animeListCall;
        if (listStatus.equals(LibraryEntryStatus.All)) {
            // get all anime
            // to do that, just don't specify a status field
            animeListCall = TenshiApp.getMal().getCurrentUserLibrary(null, sortMode, REQUEST_FIELDS, showNSFW);
        } else {
            animeListCall = TenshiApp.getMal().getCurrentUserLibrary(listStatus, sortMode, REQUEST_FIELDS, showNSFW);
        }

        // execute the call
        fetchAnimeList(animeListCall, true);
    }

    /**
     * fetch anime from MAL and
     *
     * @param call        the anime call to execute
     * @param shouldClear should the current list of anime be cleared?
     */
    private void fetchAnimeList(@NonNull Call<UserLibraryList> call, boolean shouldClear) {
        // execute the call
        call.enqueue(new Callback<UserLibraryList>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<UserLibraryList> call, Response<UserLibraryList> response) {
                // callback to TenshiApp global reauth
                if (isAdded())
                    TenshiApp.malReauthCallback(requireActivity(), response);

                if (response.isSuccessful()) {
                    // get response
                    animeListResponse = response.body();
                    if (notNull(animeListResponse)) {

                        // get anime list from response
                        List<UserLibraryEntry> newAnimeList = animeListResponse.items;

                        // clear previously loaded anime list
                        if (shouldClear)
                            animeList.clear();

                        // add newly loaded anime to list
                        animeList.addAll(newAnimeList);
                        animeListAdapter.notifyDataSetChanged();

                        // insert loaded anime into the db
                        TenshiApp.getDB().animeDB().insertLibraryAnime(newAnimeList);

                        // hide loading indicator
                        b.animeListLoadingIndicator.hide();
                    }
                } else if (response.code() == 401 && isAdded())
                    Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<UserLibraryList> call, Throwable t) {
                Log.e("Tenshi", t.toString());
                if (isAdded())
                    Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * open the details of a anime
     *
     * @param animeId the id of the anime to open
     * @param view    the view to use for the transition
     */
    private void openDetails(int animeId, @Nullable View view) {
        final Intent i = new Intent(getContext(), AnimeDetailsActivity.class);
        i.putExtra(AnimeDetailsActivity.EXTRA_ANIME_ID, animeId);

        if (view != null) {
            FrameLayout poster = view.findViewById(R.id.anime_main_poster_container);
            ActivityOptionsCompat bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), poster, poster.getTransitionName());
            startActivityForResult(i, REQUEST_ANIME_DETAILS, bundle.toBundle());
        } else
            startActivityForResult(i, REQUEST_ANIME_DETAILS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ANIME_DETAILS && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getBooleanExtra(AnimeDetailsActivity.EXTRA_ENTRY_UPDATED, false))
                fetchAnimeList();
        }
    }
}
