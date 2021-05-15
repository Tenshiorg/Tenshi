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
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.net.URLConnection;
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
import io.github.shadow578.tenshi.ui.tutorial.AnimeDetailsInLibTutorial;
import io.github.shadow578.tenshi.ui.tutorial.AnimeDetailsNoLibTutorial;
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

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.async;
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

    /**
     * request to watch the next episode.
     * in onActivityResult, if a response to this request is received, the current anime' s episode is incremented by 1 and updated
     */
    public static final int REQUEST_WATCH_NEXT_EPISODE = 12;

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

        // notify user if offline
        showSnackbarIfOffline(b.getRoot());

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
        requireUserAuthenticated();

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

        // start tutorial
        maybeStartTutorial();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if this is next episode request
        if (requestCode == REQUEST_WATCH_NEXT_EPISODE) {
            // if we already have a library status, just increment episode by 1
            // otherwise, add this anime as a new entry in Watching with 1 episode watched
            if (notNull(animeDetails.userListStatus)) {
                // get next episode in correct range 1 - n
                final int nextEpisode = Math.max(1, Math.min(withRet(animeDetails.episodesCount, 0, ec -> ec),
                        withRet(animeDetails.userListStatus, 0, ul -> ul.watchedEpisodes) + 1
                ));
                updateEntry(null, null, nextEpisode);
            } else
                updateEntry(LibraryEntryStatus.Watching, null, 1);

            // update the controls next
            updateWatchNowViews();
        }
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

        // add listener for "pin to homescreen" menu item
        menu.findItem(R.id.add_homescreen_pin).setOnMenuItemClickListener(item -> {
            addPin();
            return true;
        });

        // add listener for "select content adapter" menu item
        setupSelectContentAdapterMenuItem(menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * pin a link to this anime to the homescreen using {@link ShortcutManagerCompat}
     */
    private void addPin() {
        // make sure the anime has a valid title
        if (isNull(animeDetails) || nullOrWhitespace(animeDetails.title)) {
            Snackbar.make(b.getRoot(), R.string.details_snack_failed_to_create_pin, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // build the intent to launch
        final Intent intent = new Intent(this, AnimeDetailsActivity.class)
                .setAction(Intent.ACTION_VIEW)
                .putExtra(EXTRA_ANIME_ID, animeID);

        // build shortcut info
        final ShortcutInfoCompat pinInfo = new ShortcutInfoCompat.Builder(this, fmt("TENSHI_PIN_%d_%s", animeID, animeDetails.title))
                .setIntent(intent)
                .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher_round))
                .setShortLabel(animeDetails.title)
                .setLongLabel(animeDetails.title)
                //.setAlwaysBadged()
                .build();

        // add the pin
        if (!ShortcutManagerCompat.requestPinShortcut(this, pinInfo, null))
            Snackbar.make(b.getRoot(), R.string.details_snack_launcher_does_not_support_pins, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * maybe start the tutorial, depending on the current state
     */
    private void maybeStartTutorial() {
        // reschedule tutorial to when we finished loading the anime
        if (isNull(animeDetails)) {
            b.getRoot().postDelayed(this::maybeStartTutorial, 1000);
            return;
        }

        // check if current anime is in library
        if (notNull(animeDetails.userListStatus)) {
            // in lib
            if (!TenshiPrefs.getBool(TenshiPrefs.Key.AnimeDetailsInLibTutorialFinished, false)) {
                new AnimeDetailsInLibTutorial(this, b, b.animeWatchNowGroup.getVisibility() == View.VISIBLE)
                        .setEndListener(c -> TenshiPrefs.setBool(TenshiPrefs.Key.AnimeDetailsInLibTutorialFinished, true))
                        .start();
            }
        } else {
            // not in lib
            if (!TenshiPrefs.getBool(TenshiPrefs.Key.AnimeDetailsNoLibTutorialFinished, false)) {
                new AnimeDetailsNoLibTutorial(this, b)
                        .setEndListener(c -> TenshiPrefs.setBool(TenshiPrefs.Key.AnimeDetailsNoLibTutorialFinished, true))
                        .start();
            }
        }
    }

    /**
     * load the anime details from MAL
     */
    private void fetchAnime() {
        // load from db
        async(() -> TenshiApp.getDB().animeDB().getAnime(animeID, true), a -> {
            // ONLY set the anime IF we did not already load it from MAL
            if (notNull(a)) {
                // update last access
                async(() -> TenshiApp.getDB().accessDB().updateForAnime(a.animeId));

                // update ui
                if (isNull(animeDetails)) {
                    animeDetails = a;
                    populateViewData();
                }
            }
        });

        // request from MAL
        TenshiApp.getMal().getAnime(animeID, REQUEST_FIELDS)
                .enqueue(new Callback<Anime>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<Anime> c, Response<Anime> response) {
                        // callback to TenshiApp global reauth
                        TenshiApp.malReauthCallback(AnimeDetailsActivity.this, response);

                        if (response.isSuccessful() && response.body() != null) {
                            // call success, set data to views
                            animeDetails = response.body();
                            populateViewData();

                            // insert into db
                            async(() -> TenshiApp.getDB().animeDB().insertAnime(animeDetails));
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
        // if we watched the last episode, automatically set the status to "completed"
        // this can both happen from watch now
        // and by manually changing the episode count
        // however, do not overwrite the status if it was set somewhere else
        // OR was not set to watching before
        final int epCount = withRet(animeDetails.episodesCount, -1, ec -> ec);
        if (isNull(status)
                && notNull(watchedEpisodes)
                && watchedEpisodes >= epCount
                && notNull(animeDetails.userListStatus)
                && animeDetails.userListStatus.status == LibraryEntryStatus.Watching)
            status = LibraryEntryStatus.Completed;

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
                                updateWatchNowViews();
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

        // setup adapter for related anime/manga
        relatedMediaAdapter = new RelatedMediaAdapter(getApplicationContext(), relatedMedia,
                (v, related) -> openDetails(related.relatedAnime.animeId, related.relatedAnime.mediaType));
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
        String[] episodesItems = {getString(R.string.details_status_episodes_progress_no_episodes)};
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
            // or the anime is not in the library
            // TODO allow watching of anime that are NOT in the library -> add them after the first episode
            if (manager.getAdapterCount() <= 0
                    || isNull(animeDetails.episodesCount)
                    || animeDetails.episodesCount <= 0
                    || isNull(animeDetails.userListStatus)) {
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

            // setup short click listener == watch next episode + auto- update MAL
            b.animeWatchNowButton.setOnClickListener(v -> {
                // find the next episode
                final int nextEp = withRet(animeDetails.userListStatus, 0, ul -> ul.watchedEpisodes) + 1;

                // show a popup if the next episode is higher than the total episode count
                if (nextEp > animeDetails.episodesCount) {
                    showAnimeWatchEpisodeSelection(v, episodesAdapter);
                    return;
                }

                // open the player with the next episode
                openPlayer(nextEp, true);
            });

            // setup long click listener == select episode
            b.animeWatchNowButton.setOnLongClickListener(v -> {
                showAnimeWatchEpisodeSelection(v, episodesAdapter);
                return true;
            });

            // update views
            updateWatchNowViews();
        });
    }

    /**
     * show a popout window for episode selection.
     * on item selection, call openPlayer with the selected episode index (+1)
     *
     * @param v               the view to anchor to
     * @param episodesAdapter the episode adapter. index 0 == episode 1, ...
     */
    private void showAnimeWatchEpisodeSelection(@NonNull View v, @NonNull ArrayAdapter<String> episodesAdapter) {
        // setup the popup
        sharedListPopout.setAnchorView(v);
        sharedListPopout.setAdapter(episodesAdapter);

        // use as much width as the content requires
        sharedListPopout.setWidth(ViewsHelper.measureContentWidth(this, episodesAdapter));

        // setup onclick listener
        sharedListPopout.setOnItemClickListener((px, vx, episode, ix) -> {
            // open player and dismiss popout
            openPlayer(episode + 1, false);
            sharedListPopout.dismiss();
        });
        sharedListPopout.show();
    }

    /**
     * update the "watch now" views
     */
    private void updateWatchNowViews() {
        // get selected content adapter
        async(() -> {
            final String un = TenshiApp.getDB().contentAdapterDB().getSelectionFor(animeID);
            return TenshiApp.getContentAdapterManager().getAdapterOrDefault(un);
        }, contentAdapter -> {
            // unbox watched episode count safely and clamp to the actual possible range of episodes (1 - n)
            // normally, this should never be null (checked elsewhere), but better be safe then sorry
            final int nextEpisode = Math.max(1, Math.min(withRet(animeDetails.episodesCount, 0, ec -> ec),
                    withRet(animeDetails.userListStatus, 0, ul -> ul.watchedEpisodes) + 1
            ));

            // update button text
            with(contentAdapter, ca ->
                    b.animeWatchNowButton.setText(fmt(this, R.string.details_watch_episode_on_fmt, nextEpisode, ca.getDisplayName())));
        });
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
                        async(() -> {
                            // set content adapter for this anime
                            TenshiApp.getDB().contentAdapterDB().setSelectionFor(animeID, uniqueName);
                            return null;
                        }, x -> {
                            // update views
                            updateWatchNowViews();
                        });
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

        // region expand synopsis logic
        final int SYNOPSIS_MINIMIZED_MAX_LINES = 5;

        // setup after view rendered once
        // currently, the synopsis textview has no maximum line count
        b.synopsis.post(() -> {
            // get current line count after render
            final int lns = b.synopsis.getLineCount();

            // set max lines of synopsis view
            b.synopsis.setMaxLines(SYNOPSIS_MINIMIZED_MAX_LINES);

            // setup expand button
            if (lns <= SYNOPSIS_MINIMIZED_MAX_LINES) {
                // hide expand button if the text fits as- is
                b.expandSynopsis.setVisibility(View.GONE);
            } else {
                // setup expand button
                b.expandSynopsis.setVisibility(View.VISIBLE);
                b.expandSynopsis.setOnClickListener(v -> {
                    if (b.synopsis.getMaxLines() <= SYNOPSIS_MINIMIZED_MAX_LINES) {
                        // expand to max size
                        b.synopsis.setMaxLines(Integer.MAX_VALUE);
                        b.expandSynopsis.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_keyboard_arrow_up_24));
                    } else {
                        // collapse
                        b.synopsis.setMaxLines(SYNOPSIS_MINIMIZED_MAX_LINES);
                        b.expandSynopsis.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_keyboard_arrow_down_24));
                    }
                });
            }
        });
        //endregion

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
        if (!nullOrEmpty(animeDetails.openingThemes)) {
            b.openingThemeRecycler.setAdapter(new AnimeThemesAdapter(this, listOf(animeDetails.openingThemes)));
            b.openingThemeTitle.setVisibility(View.VISIBLE);
            b.openingThemeRecycler.setVisibility(View.VISIBLE);
        } else {
            b.openingThemeTitle.setVisibility(View.GONE);
            b.openingThemeRecycler.setVisibility(View.GONE);
        }

        // eds
        if (!nullOrEmpty(animeDetails.endingThemes)) {
            b.endingThemesRecycler.setAdapter(new AnimeThemesAdapter(this, listOf(animeDetails.endingThemes)));
            b.endingThemesTitle.setVisibility(View.VISIBLE);
            b.endingThemesRecycler.setVisibility(View.VISIBLE);
        } else {
            b.endingThemesTitle.setVisibility(View.GONE);
            b.endingThemesRecycler.setVisibility(View.GONE);
        }

        //related
        if (!nullOrEmpty(animeDetails.relatedAnime) || !nullOrEmpty(animeDetails.relatedManga)) {
            relatedMedia.addAll(listOf(animeDetails.relatedAnime, animeDetails.relatedManga));
            b.relatedMediaTitle.setVisibility(View.VISIBLE);
            b.relatedMediaRecycler.setVisibility(View.VISIBLE);
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
     * @param episode          the selected episode. used for title
     * @param watchNextEpisode if set, the player activity is started with {@link #REQUEST_WATCH_NEXT_EPISODE} (and thus auto- increments the episode after watching)
     */
    private void openPlayer(int episode, boolean watchNextEpisode) {
        // abort if no adapters available
        if (TenshiApp.getContentAdapterManager().getAdapterCount() <= 0)
            return;

        // get content adapter
        async(() -> {
            final String un = TenshiApp.getDB().contentAdapterDB().getSelectionFor(animeID);
            return TenshiApp.getContentAdapterManager().getAdapterOrDefault(un);
        }, contentAdapter -> {
            // make sure we have all infos for the call
            if (isNull(animeDetails)
                    || isNull(animeDetails.title)
                    || isNull(animeDetails.titleSynonyms)
                    || isNull(contentAdapter)) {
                Snackbar.make(b.getRoot(), R.string.details_snack_content_empty_response, Snackbar.LENGTH_SHORT).show();
                return;
            }

            // bind the adapter and request playback url
            contentAdapter.bind(this);
            contentAdapter.requestStreamUri(animeID, animeDetails.title, animeDetails.titleSynonyms.jp, episode,
                    uriStr -> {
                        Uri uri;
                        if (notNull(uriStr) && (uri = Uri.parse(uriStr)) != null) {
                            //open in player
                            final Intent playIntent = new Intent(Intent.ACTION_VIEW);
                            String guessedType = URLConnection.guessContentTypeFromName(uriStr);
                            if (nullOrWhitespace(guessedType))
                                guessedType = "video/*";

                            playIntent.setDataAndTypeAndNormalize(uri, guessedType);

                            // set title extras
                            // bc VLC has to be extra -_-
                            final String title = fmt(this, R.string.details_watch_intent_title_fmt, animeDetails.title, episode);
                            playIntent.putExtra(Intent.EXTRA_TITLE, title);
                            playIntent.putExtra("title", title);

                            // start activity
                            if (watchNextEpisode)
                                startActivityForResult(playIntent, REQUEST_WATCH_NEXT_EPISODE);
                            else
                                startActivity(playIntent);
                        } else {
                            // did not return a url, show error snackbar
                            Snackbar.make(b.getRoot(), R.string.details_snack_content_empty_response, Snackbar.LENGTH_SHORT).show();
                        }
                        // unbind adapter
                        contentAdapter.unbind(this);
                    });
        });
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
