package io.github.shadow578.tenshi.notifications.workers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.db.TenshiDB;
import io.github.shadow578.tenshi.mal.model.UserLibraryEntry;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.mal.model.type.LibrarySortMode;
import io.github.shadow578.tenshi.notifications.TenshiNotificationChannel;
import io.github.shadow578.tenshi.notifications.TenshiNotificationManager;
import io.github.shadow578.tenshi.notifications.db.SentNotificationsDB;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;

/**
 * base class for notification- related background workers
 */
public abstract class WorkerBase extends Worker {

    /**
     * the database instance.
     */
    private TenshiDB db = null;

    /**
     * notification database instance
     */
    private SentNotificationsDB notificationsDB = null;

    /**
     * notification manager instance.
     */
    private TenshiNotificationManager notifyManager = null;

    public WorkerBase(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!shouldRun())
            return Result.success();

        return run();
    }

    /**
     * wrapped function for {@link Worker#doWork()}.
     * only runs if {@link #shouldRun()} is true
     *
     * @return work result
     */
    protected abstract Result run();

    /**
     * @return should this worker run?
     */
    protected abstract boolean shouldRun();

    /**
     * load all anime with the given status from the database
     *
     * @param db       the database instance
     * @param statuses the statuses to load
     * @param nsfw     include NSFW entries?
     * @return the list of all loaded entries
     */
    @NonNull
    protected List<UserLibraryEntry> getEntries(@NonNull TenshiDB db, @NonNull List<LibraryEntryStatus> statuses, boolean nsfw) {
        final ArrayList<UserLibraryEntry> entries = new ArrayList<>();
        for (LibraryEntryStatus status : statuses) {
            // load from DB and add
            final List<UserLibraryEntry> e = db.animeDB().getUserLibrary(status, LibrarySortMode.anime_id, nsfw);
            if (!nullOrEmpty(e))
                entries.addAll(e);
        }
        return entries;
    }

    /**
     * creates a pending intent to open the details page of a anime
     *
     * @param animeId the anime ID
     * @return the pending intent for opening the details page
     */
    @NonNull
    protected PendingIntent getDetailsOpenIntent(int animeId) {
        // create intent to open details
        final Intent detailsIntent = new Intent(getApplicationContext(), AnimeDetailsActivity.class);
        detailsIntent.putExtra(AnimeDetailsActivity.EXTRA_ANIME_ID, animeId);

        // create pending intent
        return PendingIntent.getActivity(getApplicationContext(), animeId, detailsIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * initialize TenshiPrefs
     */
    protected void requirePrefs() {
        TenshiPrefs.init(getApplicationContext());
    }

    /**
     * get the tenshi anime database instance.
     * if the app is not running ({@link TenshiApp#getDB()} not possible) this initializes the database on the first call.
     *
     * @return the database instance
     */
    @NonNull
    protected TenshiDB getDB() {
        if (notNull(db))
            return db;

        // try to get database from TenshiApp first
        if (notNull(TenshiApp.INSTANCE)) {
            db = TenshiApp.getDB();
            return db;
        }

        // app not running, create on demand
        db = TenshiDB.create(getApplicationContext());
        return db;
    }

    /**
     * get the notification database instance.
     * database is created on demand
     *
     * @return the database instance
     */
    @NonNull
    protected SentNotificationsDB getNotifyDB() {
        if (notNull(notificationsDB))
            return notificationsDB;

        // try to get database from TenshiApp first
        if (notNull(TenshiApp.INSTANCE)) {
            notificationsDB = TenshiApp.getNotifyDB();
            return notificationsDB;
        }

        // app not running, create on demand
        notificationsDB = SentNotificationsDB.create(getApplicationContext());
        return notificationsDB;
    }

    /**
     * get the tenshi notification manager instance.
     * if the app is not running ({@link TenshiApp#getNotifyManager()} ()} not possible) this initializes the manager on the first call.
     *
     * @return the notification manager
     */
    @NonNull
    protected TenshiNotificationManager getNotifyManager() {
        if (notNull(notifyManager))
            return notifyManager;

        // try to get manager from TenshiApp first
        if (notNull(TenshiApp.INSTANCE)) {
            notifyManager = TenshiApp.getNotifyManager();
            return notifyManager;
        }

        // app not running, create on demand
        notifyManager = new TenshiNotificationManager(getApplicationContext());

        // also have to register notification channels
        TenshiNotificationChannel.registerAll(getApplicationContext(), (value, channel) -> true);
        return notifyManager;
    }
}
