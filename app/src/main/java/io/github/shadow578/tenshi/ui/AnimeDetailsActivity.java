package io.github.shadow578.tenshi.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.adapter.AnimeThemesAdapter;
import io.github.shadow578.tenshi.adapter.RelatedMediaAdapter;
import io.github.shadow578.tenshi.databinding.ActivityAnimeDetailsBinding;
import io.github.shadow578.tenshi.extensionslib.content.ContentAdapterWrapper;
import io.github.shadow578.tenshi.mal.MalApiHelper;
import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.LibraryStatus;
import io.github.shadow578.tenshi.mal.model.RelatedMedia;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.mal.model.type.MediaType;
import io.github.shadow578.tenshi.util.CustomTabsHelper;
import io.github.shadow578.tenshi.util.DateHelper;
import io.github.shadow578.tenshi.util.GlideHelper;
import io.github.shadow578.tenshi.util.LocalizationHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;
import io.github.shadow578.tenshi.util.ViewsHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.cast;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.concat;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.elvis;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.elvisEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.foreach;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.join;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.listOf;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrWhitespace;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.repeat;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.str;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.withRet;

/**
 * activity for viewing anime details and updating the library status.
 * If the library entry was updated, the return intent has the EXTRA_ENTRY_UPDATED set to true
 */
public class AnimeDetailsActivity extends TenshiActivity {
    /**
     * extra to tell the activity what anime to show
     */
    public static final String EXTRA_ANIME_ID = "animeID";

    /**
     * extra put on the return intent if the entry was updated
     */
    public static final String EXTRA_ENTRY_UPDATED = "entryUpdated";

    private static final int FALLBACK_ANIME = 38759; // You may need this if you get this :P
    private static final Pattern URL_ID_PATTERN = Pattern.compile("(?:https|http)://(?:www.)*myanimelist.net/anime/(\\d*)");
    private static final String REQUEST_FIELDS = MalApiHelper.getQueryableFields(Anime.class);

    private final List<RelatedMedia> relatedMedia = new ArrayList<>();
    private ActivityAnimeDetailsBinding b;
    private Anime animeDetails;
    private int animeID;
    private RelatedMediaAdapter relatedMediaAdapter;
    private ListPopupWindow sharedListPopout;
    private boolean wasUpdated = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAnimeDetailsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // check user is logged in, redirect to login and finish() if not
        requireUserAuthenticated();

        //enable decor (globally disabled in TenshiActivity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        }

        // setup action bar
        setSupportActionBar(b.animeDetailsToolbar);
        with(getSupportActionBar(), sab -> {
            sab.setDisplayHomeAsUpEnabled(true);
            sab.setHomeButtonEnabled(true);
        });
        b.animeDetailsToolbar.setNavigationOnClickListener(v -> onBackPressed());

        // make sure we're logged in
        if (!TenshiApp.isUserAuthenticated()) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        // get anime id from intent extra first
        animeID = getIntent().getIntExtra(EXTRA_ANIME_ID, -1);

        // if we were called by opening a mal link, get the anime's id from the url
        animeID = withRet(getIntent().getDataString(), animeID, data -> {
            Matcher m = URL_ID_PATTERN.matcher(data);
            if (m.matches())
                return tryParseInt(m.group(1), FALLBACK_ANIME);
            else
                return -1;
        });

        // if animeID still is -1, use the fallback anime and give the user a info
        if (animeID == -1) {
            animeID = FALLBACK_ANIME;
            Snackbar.make(b.getRoot(), R.string.details_snack_error_no_id, Snackbar.LENGTH_SHORT).show();
        }

        // do init
        setupViews();

        // fetch data from mal
        fetchAnime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (animeDetails != null)
            b.loadingIndicator.setVisibility(View.GONE);
    }

    @Override
    public void finish() {
        // notify AnimeList Fragment that we changed a entry and it has to reload
        final Intent retIntent = new Intent();
        retIntent.putExtra(EXTRA_ENTRY_UPDATED, wasUpdated);
        setResult(RESULT_OK, retIntent);
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu
        getMenuInflater().inflate(R.menu.anime_details_menu, menu);

        // create url for this anime
        //TODO: hardcoded url
        final String malUrl = "https://myanimelist.net/anime/" + animeID;

        // add listener for "view on mal" menu item
        menu.findItem(R.id.view_on_mal_btn).setOnMenuItemClickListener(item -> {
            CustomTabsHelper.openInCustomTab(this, malUrl);
            return true;
        });

        // add listener for "share" menu item
        menu.findItem(R.id.share).setOnMenuItemClickListener(item -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, malUrl);
            startActivity(Intent.createChooser(i, animeDetails.title));
            return true;
        });

        // add listener for "select content adapter" menu item
        setupSelectContentAdapterMenuItem(menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * load the anime details from MAL
     */
    private void fetchAnime() {
        TenshiApp.getMal().getAnime(animeID, REQUEST_FIELDS)
                .enqueue(new Callback<Anime>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<Anime> c, Response<Anime> response) {
                        // callback to TenshiApp global reauth
                        TenshiApp.malReauthCallback(AnimeDetailsActivity.this, response);

                        if (response.isSuccessful() && response.body() != null) {
                            // call success
                            animeDetails = response.body();
                            populateViewData();
                        } else if (response.code() == 404) {
                            // anime not found, use fallback
                            c.cancel();

                            // only if not already on fallback
                            if (animeID != FALLBACK_ANIME) {
                                animeID = FALLBACK_ANIME;
                                fetchAnime();
                            }
                        } else if (response.code() == 401)
                            Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<Anime> c, Throwable t) {
                        Log.e("Tenshi", t.toString());
                        Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * update the MAL entry
     *
     * @param status          the updated status. null if not changed
     * @param score           the updated score. null if not changed
     * @param watchedEpisodes the updated number of episodes watched. null if not changed
     */
    private void updateEntry(@Nullable LibraryEntryStatus status, @Nullable Integer score, @Nullable Integer watchedEpisodes) {
        // check if anything changed (at least one value not null)
        if (notNull(status) || score != null || watchedEpisodes != null) {
            // do update call
            TenshiApp.getMal().updateListEntry(animeID,
                    status, score, watchedEpisodes)
                    .enqueue(new Callback<LibraryStatus>() {
                        @Override
                        @EverythingIsNonNull
                        public void onResponse(Call<LibraryStatus> c, Response<LibraryStatus> response) {
                            // callback to TenshiApp global reauth
                            TenshiApp.malReauthCallback(AnimeDetailsActivity.this, response);

                            if (response.isSuccessful() && response.body() != null) {
                                // update status
                                LibraryStatus listStatus = response.body();
                                animeDetails.userListStatus = listStatus;
                                updateStatusViewData(listStatus);
                                updateLibraryControls();
                                wasUpdated = true;

                                // show a snackbar to user
                                Snackbar.make(b.getRoot(), R.string.details_snack_entry_updated, Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(b.getRoot(), R.string.details_snack_entry_update_error, Snackbar.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        @EverythingIsNonNull
                        public void onFailure(Call<LibraryStatus> c, Throwable t) {
                            Log.e("Tenshi", t.toString());
                            Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // no change
            Snackbar.make(b.getRoot(), R.string.details_snack_update_no_changes, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * delete this entry from the current user's library
     */
    private void deleteEntry() {
        TenshiApp.getMal().deleteListEntry(animeID)
                .enqueue(new Callback<Void>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        // callback to TenshiApp global reauth
                        TenshiApp.malReauthCallback(AnimeDetailsActivity.this, response);

                        if (response.isSuccessful()) {
                            animeDetails.userListStatus = null;
                            wasUpdated = true;
                            updateLibraryControls();
                            Snackbar.make(b.getRoot(), R.string.details_snack_entry_removed, Snackbar.LENGTH_SHORT).show();
                        } else
                            Snackbar.make(b.getRoot(), R.string.details_snack_entry_remove_error, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("Tenshi", t.toString());
                        Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * initialize the views
     */
    private void setupViews() {
        // allow copying of the title by clicking
        b.animeMainTitle.setOnClickListener(v -> copyToClip(b.animeMainTitle.getText().toString()));

        // init shared popout window
        sharedListPopout = new ListPopupWindow(this, null, R.attr.listPopupWindowStyle);

        // expand synopsis
        b.expandSynopsis.setOnClickListener(v -> {
            if (b.synopsis.getMaxLines() == 5) {
                // expand
                b.synopsis.setMaxLines(Integer.MAX_VALUE);
                b.expandSynopsis.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_keyboard_arrow_up_24));
            } else {
                // subtract
                b.synopsis.setMaxLines(5);
                b.expandSynopsis.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_keyboard_arrow_down_24));
            }
        });

        // setup adapter for related anime/manga
        relatedMediaAdapter = new RelatedMediaAdapter(getApplicationContext(), relatedMedia, (v, related) -> openDetails(related.relatedAnime.animeId, related.relatedAnime.mediaType));
        b.relatedMediaRecycler.setAdapter(relatedMediaAdapter);
    }

    /**
     * initialize the views used for library status
     */
    private void setupListStatusViews() {
        // create adapter for status dropdown
        final String[] statusItems = {
                getString(R.string.list_status_plan_to_watch),
                getString(R.string.list_status_watching),
                getString(R.string.list_status_completed),
                getString(R.string.list_status_on_hold),
                getString(R.string.list_status_dropped)
        };
        final LibraryEntryStatus[] statusValues = {
                LibraryEntryStatus.PlanToWatch,
                LibraryEntryStatus.Watching,
                LibraryEntryStatus.Completed,
                LibraryEntryStatus.OnHold,
                LibraryEntryStatus.Dropped
        };
        final ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, R.layout.recylcer_generic_text, statusItems);

        // create adapter for rating
        final String[] ratingItems = repeat(0, 10, score -> LocalizationHelper.localizeScore(score, this)).toArray(new String[0]);
        final ArrayAdapter<String> ratingsAdapter = new ArrayAdapter<>(this, R.layout.recylcer_generic_text, ratingItems);

        // create adapter for episodes
        final boolean hasEpisodes = notNull(animeDetails.episodesCount) && animeDetails.episodesCount > 0;
        String[] episodesItems = { getString(R.string.details_status_episodes_progress_no_episodes) };
        if (hasEpisodes)
            episodesItems = repeat(0, animeDetails.episodesCount,
                    e -> e == 0 ? getString(R.string.details_status_episode_select_none) : fmt(this, R.string.details_status_episode_select_fmt, e))
                    .toArray(new String[0]);
        final ArrayAdapter<String> episodesAdapter = new ArrayAdapter<>(this, R.layout.recylcer_generic_text, episodesItems);

        // set on click listeners
        // status
        b.animeEditListStatusBtn.setOnClickListener(v -> {
            // setup the popup
            sharedListPopout.setAnchorView(v);
            sharedListPopout.setAdapter(statusAdapter);

            // use as much width as the content requires
            sharedListPopout.setWidth(ViewsHelper.measureContentWidth(this, statusAdapter));

            // setup onclick listener
            sharedListPopout.setOnItemClickListener((px, vx, statusIndex, ix) -> {
                // update anime and dismiss
                updateEntry(statusValues[statusIndex], null, null);
                sharedListPopout.dismiss();
            });
            sharedListPopout.show();
        });

        // rating
        b.animeEditRatingBtn.setOnClickListener(v -> {
            // setup the popup
            sharedListPopout.setAnchorView(v);
            sharedListPopout.setAdapter(ratingsAdapter);

            // use as much width as the content requires
            sharedListPopout.setWidth(ViewsHelper.measureContentWidth(this, ratingsAdapter));

            // setup onclick listener
            sharedListPopout.setOnItemClickListener((px, vx, rating, ix) -> {
                // update anime and dismiss
                updateEntry(null, rating, null);
                sharedListPopout.dismiss();
            });
            sharedListPopout.show();
        });

        // episode count (disable list onclick action when no animes are available)
        b.animeEditEpisodeCountBtn.setOnClickListener(v -> {
            // setup the popup
            sharedListPopout.setAnchorView(v);
            sharedListPopout.setAdapter(episodesAdapter);

            // use as much width as the content requires
            sharedListPopout.setWidth(ViewsHelper.measureContentWidth(this, episodesAdapter));

            // setup onclick listener
            sharedListPopout.setOnItemClickListener((px, vx, episode, ix) -> {
                // update anime and dismiss
                // only update if we have episodes (otherwise the list is just one entry "No Episodes available")
                if (hasEpisodes)
                    updateEntry(null, null, episode);
                sharedListPopout.dismiss();
            });
            sharedListPopout.show();
        });

        // add listener for add to list button
        b.animeAddToListBtn.setOnClickListener(v -> addEntry());

        // add listener for delete button
        b.detailsDeleteButton.setOnClickListener(v -> deleteEntry());

        // update status data
        with(animeDetails.userListStatus, this::updateStatusViewData);

        // update list controls
        updateLibraryControls();
    }

    /**
     * update the data of the library status views
     *
     * @param newStatus the new status
     */
    private void updateStatusViewData(@NonNull LibraryStatus newStatus) {
        b.animeEditListStatusBtn.setText(LocalizationHelper.localizeLibraryStatus(newStatus.status, this));
        b.animeEditEpisodeCountBtn.setText(fmt(this, R.string.details_status_episodes_progress_fmt, newStatus.watchedEpisodes, animeDetails.episodesCount));
        b.animeEditRatingBtn.setText(LocalizationHelper.localizeScore(elvis(newStatus.score, 0), this));
    }

    /**
     * add this anime to the user's library
     */
    private void addEntry() {
        updateEntry(LibraryEntryStatus.PlanToWatch, null, null);
    }

    /**
     * update the visibility of the "add to library" and library status controls
     */
    private void updateLibraryControls() {
        final boolean added = notNull(animeDetails.userListStatus);

        // if this is a entry in our MAL list, show status update controls. otherwise, show add button
        b.animeEditGroup.setVisibility(added ? View.VISIBLE : View.GONE);
        b.animeAddToListGroup.setVisibility(added ? View.GONE : View.VISIBLE);

        // show delete button only if in our MAL list
        b.detailsDeleteGroup.setVisibility(added ? View.VISIBLE : View.GONE);
    }

    /**
     * setup the watch now controls (and connect
     */
    private void setupWatchNowControls() {
        // wait until discovery finsihed
        TenshiApp.getContentAdapterManager().addOnDiscoveryEndCallback(manager -> {
            // hide controls if no content adapters were discovered
            // or if the anime does not have any episodes yet
            if (manager.getAdapterCount() <= 0
                    || isNull(animeDetails.episodesCount)
                    || animeDetails.episodesCount <= 0) {
                b.animeWatchNowGroup.setVisibility(View.GONE);
                b.divWatchNowSynopsis.setVisibility(View.GONE);
                return;
            }

            // make controls visible
            b.animeWatchNowGroup.setVisibility(View.VISIBLE);
            b.divWatchNowSynopsis.setVisibility(View.VISIBLE);

            // create adapter for episodes
            final String[] episodesItems = repeat(1, animeDetails.episodesCount,
                    e -> fmt(this, R.string.details_status_episode_select_fmt, e))
                    .toArray(new String[0]);
            final ArrayAdapter<String> episodesAdapter = new ArrayAdapter<>(this, R.layout.recylcer_generic_text, episodesItems);

            // setup click listener for watch now button
            b.animeWatchNowButton.setOnClickListener(v -> {
                // setup the popup
                sharedListPopout.setAnchorView(v);
                sharedListPopout.setAdapter(episodesAdapter);

                // use as much width as the content requires
                sharedListPopout.setWidth(ViewsHelper.measureContentWidth(this, episodesAdapter));

                // setup onclick listener
                sharedListPopout.setOnItemClickListener((px, vx, episode, ix) -> {
                    // open player and dismiss popout
                    openPlayer(episode + 1);
                    sharedListPopout.dismiss();
                });
                sharedListPopout.show();
            });

            // update views
            updateWatchNowViews();
        });
    }

    /**
     * update the "watch now" views
     */
    private void updateWatchNowViews() {
        // update button text
        final ContentAdapterWrapper contentAdapter = getSelectedContentAdapter();
        with(contentAdapter, ca ->
                b.animeWatchNowButton.setText(fmt(this, R.string.details_watch_on_fmt, ca.getDisplayName())));
    }

    /**
     * set up the "Select content adapter" option in the top menu
     *
     * @param menu the menu to create the items in
     */
    private void setupSelectContentAdapterMenuItem(@NonNull Menu menu) {
        // wait until discovery finsihed
        TenshiApp.getContentAdapterManager().addOnDiscoveryEndCallback(manager -> {

            // dont add submenu when no adapters are available
            if (manager.getAdapterCount() <= 0)
                return;

            // create submenu for content adapter selection
            SubMenu sub = menu.addSubMenu(R.string.details_choose_content_provider_menu);

            // create a item for every content adapter
            for (ContentAdapterWrapper ca : manager.getAdapters()) {
                // get display and unique name
                final String displayName = ca.getDisplayName();
                final String uniqueName = ca.getUniqueName();

                // only add to selection if both are not empty
                if (!nullOrEmpty(displayName) && !nullOrEmpty(uniqueName))
                    sub.add(displayName).setOnMenuItemClickListener(item -> {
                        // set content adapter for this anime
                        TenshiPrefs.setString(TenshiPrefs.Key.AnimeSelectedContentProvider, "" + animeID, uniqueName);

                        // update views
                        updateWatchNowViews();
                        return true;
                    });
            }
        });
    }

    /**
     * populate the data of the views
     */
    private void populateViewData() {
        final String unknown = getString(R.string.shared_unknown);
        final String noValue = getString(R.string.shared_no_value);

        // hide loading bar
        b.loadingIndicator.hide();

        // update controls action
        updateLibraryControls();

        // load data into views:
        // poster
        GlideHelper.glide(this, animeDetails.poster != null ? animeDetails.poster.mediumUrl : "").into(b.animeMainPoster);

        b.animeMainPoster.setOnClickListener(v -> openPoster());

        // title
        b.animeMainTitle.setText(animeDetails.title);

        // media type
        if (notNull(animeDetails.mediaType))
            b.animeMediaType.setText(LocalizationHelper.localizeMediaType(animeDetails.mediaType, this));
        else
            b.animeMediaType.setText(unknown);

        // media status
        if (notNull(animeDetails.broadcastStatus))
            b.animeMediaType.setText(LocalizationHelper.localizeBroadcastStatus(animeDetails.broadcastStatus, this));
        else
            b.animeMediaType.setText(unknown);

        //synopsis
        with(animeDetails.synopsis, unknown, p -> b.synopsis.setText(p));

        // genre chips
        b.genresChips.removeAllViews();
        foreach(animeDetails.genres, p -> {
            Chip chip = new Chip(b.genresChips.getContext());
            chip.setText(p.name);
            b.genresChips.addView(chip);
        });

        // additional info:
        // alternative titles
        if (notNull(animeDetails.titleSynonyms)) {
            // synonyms
            boolean hasSynonyms = !nullOrEmpty(animeDetails.titleSynonyms.synonyms);
            if (hasSynonyms)
                b.titleSynonyms.setText(join(",\n", animeDetails.titleSynonyms.synonyms, s -> s));
            else
                b.titleSynonymsGroup.setVisibility(View.GONE);

            // jp title
            boolean hasJpTitle = !nullOrWhitespace(animeDetails.titleSynonyms.jp);
            if (hasJpTitle)
                b.titleJp.setText(animeDetails.titleSynonyms.jp);
            else
                b.titleJpGroup.setVisibility(View.GONE);
        } else {
            // no alternative titles, hide those views
            b.titleSynonymsGroup.setVisibility(View.GONE);
            b.titleJpGroup.setVisibility(View.GONE);
        }

        // start date
        b.startDate.setText(DateHelper.format(animeDetails.startDate, noValue));

        // end date
        b.endDate.setText(DateHelper.format(animeDetails.endDate, noValue));

        // year & season
        b.season.setText(elvisEmpty(withRet(animeDetails.startSeason,
                p -> concat(LocalizationHelper.localizeSeason(p.season, this), " ", str(p.year))),
                noValue));

        // broadcast time
        if (animeDetails.broadcastInfo != null
                && notNull(animeDetails.broadcastInfo.weekday)
                && notNull(animeDetails.broadcastInfo.startTime))
            b.broadcast.setText(concat(LocalizationHelper.localizeWeekday(animeDetails.broadcastInfo.weekday, this), " ", DateHelper.format(animeDetails.broadcastInfo.startTime, unknown)));


        // episode duration
        b.duration.setText(elvisEmpty(withRet(animeDetails.averageEpisodeDuration,
                p -> fmt(this, R.string.details_anime_duration_minutes_fmt, p / 60)),
                noValue));

        // source
        if (notNull(animeDetails.originalSource))
            b.source.setText(LocalizationHelper.localizeSource(animeDetails.originalSource, this));
        else
            b.source.setText(noValue);

        // studios
        b.studios.setText(elvisEmpty(join(",\n", animeDetails.studios, p -> p.name), unknown));

        // ops
        if (!nullOrEmpty(animeDetails.openingThemes))
            b.openingThemeRecycler.setAdapter(new AnimeThemesAdapter(this, listOf(animeDetails.openingThemes)));
        else {
            b.openingThemeTitle.setVisibility(View.GONE);
            b.openingThemeRecycler.setVisibility(View.GONE);
        }

        // eds
        if (!nullOrEmpty(animeDetails.endingThemes))
            b.endingThemesRecycler.setAdapter(new AnimeThemesAdapter(this, listOf(animeDetails.endingThemes)));
        else {
            b.endingThemesTitle.setVisibility(View.GONE);
            b.endingThemesRecycler.setVisibility(View.GONE);
        }

        //related
        if (!nullOrEmpty(animeDetails.relatedAnime) || !nullOrEmpty(animeDetails.relatedManga)) {
            relatedMedia.addAll(listOf(animeDetails.relatedAnime, animeDetails.relatedManga));
            relatedMediaAdapter.notifyDataSetChanged();
        } else {
            b.relatedMediaTitle.setVisibility(View.GONE);
            b.relatedMediaRecycler.setVisibility(View.GONE);
        }

        // setup list status controls
        setupListStatusViews();

        // setup watch now
        setupWatchNowControls();
    }

    /**
     * get the playback url from the selected or default adapter and start playback.
     *
     * @param episode the selected episode. used for title
     */
    private void openPlayer(int episode) {
        // abort if no adapters available
        if (TenshiApp.getContentAdapterManager().getAdapterCount() <= 0)
            return;

        // get content adapter
        final ContentAdapterWrapper contentAdapterF = getSelectedContentAdapter();

        // make sure we have all infos for the call
        if (isNull(animeDetails)
                || isNull(animeDetails.title)
                || isNull(animeDetails.titleSynonyms)
                || isNull(contentAdapterF)) {
            Snackbar.make(b.getRoot(), R.string.details_snack_content_empty_response, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // bind the adapter and request playback url
        contentAdapterF.bind(this);
        contentAdapterF.requestStreamUri(animeID, animeDetails.title, animeDetails.titleSynonyms.jp, episode,
                uriStr -> {
                    Uri uri;
                    if (notNull(uriStr) && (uri = Uri.parse(uriStr)) != null) {
                        //open in player
                        final Intent playIntent = new Intent(Intent.ACTION_VIEW);
                        playIntent.setData(uri);
                        playIntent.putExtra(Intent.EXTRA_TITLE, fmt(this, R.string.details_watch_intent_title_fmt, animeDetails.title, episode));
                        startActivity(playIntent);
                    } else {
                        // did not return a url, show error snackbar
                        Snackbar.make(b.getRoot(), R.string.details_snack_content_empty_response, Snackbar.LENGTH_SHORT).show();
                    }
                    // unbind adapter
                    contentAdapterF.unbind(this);
                });
    }

    /**
     * get the selected content adapter for this anime.
     * if no preference is saved, or it is invalid, uses the first adapter
     *
     * @return the content adapter
     */
    @Nullable
    private ContentAdapterWrapper getSelectedContentAdapter() {
        // get selected content adapter for this anime
        final String selectedAdapter = TenshiPrefs.getString(TenshiPrefs.Key.AnimeSelectedContentProvider, "" + animeID, "");
        return TenshiApp.getContentAdapterManager().getAdapterOrDefault(selectedAdapter);
    }

    /**
     * open the poster of the anime in a FullscreenImageActivity
     */
    private void openPoster() {
        // prepare intent and transition
        final Intent i = new Intent(this, FullscreenImageActivity.class);
        final ActivityOptionsCompat transitionOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                b.animeMainPoster,
                b.animeMainPoster.getTransitionName());

        // put poster url into extra, preferred large
        final String posterUrl = withRet(animeDetails.poster, p -> elvisEmpty(p.largeUrl, p.mediumUrl));

        // start activity if we found a poster
        if (!nullOrEmpty(posterUrl)) {
            i.putExtra(FullscreenImageActivity.EXTRA_IMAGE_URL, posterUrl);
            startActivity(i, transitionOptions.toBundle());
        }
    }

    /**
     * open the details of another anime or manga.
     * manga are opened in a chrome custom tab
     *
     * @param id        the id of the anime/manga
     * @param mediaType the media type
     */
    private void openDetails(int id, @Nullable MediaType mediaType) {
        if (notNull(mediaType)) {
            switch (mediaType) {
                case Manga:
                case OneShot:
                case Manhwa:
                case Manhua:
                case Novel:
                case LightNovel:
                case Doujinshi:
                    // manga or whatever, open mal page
                    //TODO hardcoded url
                    CustomTabsHelper.openInCustomTab(this, "https://myanimelist.net/manga/" + id);
                default:
                    // probably anime, open details activity
                    Intent i = new Intent(this, AnimeDetailsActivity.class);
                    i.putExtra(EXTRA_ANIME_ID, id);
                    i.putExtra("defaultTransition", true);
                    startActivity(i);
            }
        }
    }

    /**
     * copy a message to the clipboard
     *
     * @param text the text to copy to clipboard
     */
    private void copyToClip(@NonNull String text) {
        final ClipboardManager clip = cast(getSystemService(CLIPBOARD_SERVICE));
        final ClipData data = ClipData.newPlainText("title", text);

        if (notNull(clip)) {
            clip.setPrimaryClip(data);
            Toast.makeText(this, R.string.shared_toast_copied_to_clip, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * try to parse a integer
     *
     * @param s   the string to parse
     * @param def the default value if parsing fails
     * @return the parsed value, or def if failed
     */
    @SuppressWarnings("SameParameterValue")
    private int tryParseInt(@Nullable String s, int def) {
        try {
            if (isNull(s))
                return def;

            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
