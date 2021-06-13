package io.github.shadow578.tenshi.notifications.workers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.db.TenshiDB;
import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.BroadcastInfo;
import io.github.shadow578.tenshi.mal.model.RelatedMedia;
import io.github.shadow578.tenshi.mal.model.UserLibraryEntry;
import io.github.shadow578.tenshi.mal.model.type.BroadcastStatus;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.mal.model.type.LibrarySortMode;
import io.github.shadow578.tenshi.notifications.TenshiNotificationChannel;
import io.github.shadow578.tenshi.notifications.TenshiNotificationManager;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;
import io.github.shadow578.tenshi.util.DateHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.listOf;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;

/**
 * a worker that handles sending notifications when media in the user library are updated
 */
public class MediaUpdateNotificationsWorker extends Worker {

    /**
     * unique name of the worker
     */
    private static final String UNIQUE_WORKER_NAME = "io.github.shadow578.tenshi.notifications.workers.MediaUpdateNotificationsWorker";

    public MediaUpdateNotificationsWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * register this worker. If the worker is disabled, all pending work request are canceled using {@link #cancel(Context)}
     *
     * @param ctx the context to work in
     */
    public static void register(@NonNull Context ctx) {
        // check if we should enable the worker
        // disable existing workers if not
        if (isDisabled(ctx)) {
            cancel(ctx);
            return;
        }

        // create the worker
        //TODO repeatInterval very high for testing + change in props
        final PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(MediaUpdateNotificationsWorker.class,
                30, TimeUnit.MINUTES)
                .setConstraints(getConstrains())
                .build();

        // enqueue the worker with the unique name
        WorkManager.getInstance(ctx)
                .enqueueUniquePeriodicWork(UNIQUE_WORKER_NAME, ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }

    /**
     * run the worker now, ignoring disable state and constrains.
     * (for testing)
     *
     * @param ctx the context to run in
     */
    public static void runNow(@NonNull Context ctx) {
        // create worker
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MediaUpdateNotificationsWorker.class)
                .build();

        // enqueue the worker
        WorkManager.getInstance(ctx)
                .enqueue(workRequest);
    }

    /**
     * cancel any pending work requests for this worker
     *
     * @param ctx the context to work in
     */
    public static void cancel(@NonNull Context ctx) {
        WorkManager.getInstance(ctx)
                .cancelUniqueWork(UNIQUE_WORKER_NAME);
    }

    /**
     * the database instance.
     * this should be initialized in {@link #doWork()} before anything depending on it is called.
     */
    private TenshiDB db;

    /**
     * notification manager instance.
     * this should be initialized in {@link #doWork()} before anything depending on it is called.
     */
    private TenshiNotificationManager notifyManager;

    @NonNull
    @Override
    public Result doWork() {
        // make sure we are actually supposed to run
        if (isDisabled(getApplicationContext())) {
            Log.w("Tenshi", "MediaUpdateNotificationWorker running even tho #shouldEnableWorker was false! aborting now");
            return Result.success();
        }

        try {
            //TODO: work
            // - update anime db for required categories
            // - check for currently / soon (-ish / 6h)
            //      x- airing anime in watching / plan to watch
            //      x- premiering anime in plan to watch
            //      x- premiering related anime for anime in watching / plan to watch / completed
            // ^^ categories in props

            // initialize database
            // if the app is running, use the instance from TenshiApp. otherwise, init it manually
            if (notNull(TenshiApp.INSTANCE)) {
                // we should have a db instance, use that
                db = TenshiApp.getDB();
            } else {
                // no db instance, fallback to creating one
                db = TenshiDB.create(getApplicationContext());
            }

            // initialize notification manager
            // same as for the db
            if (notNull(TenshiApp.INSTANCE)) {
                // we should have a notify manager instance, use that
                notifyManager = TenshiApp.getNotifyManager();
            } else {
                // no notify manager instance, fallback to creating one
                notifyManager = new TenshiNotificationManager(getApplicationContext());

                // also have to register channels
                TenshiNotificationChannel.registerAll(getApplicationContext(), (value, channel) -> true);
            }

            // get NSFW preference
            TenshiPrefs.init(getApplicationContext());
            final boolean showNSFW = TenshiPrefs.getBool(TenshiPrefs.Key.NSFW, false);
            // TODO: update database categories

            // check alarms
            checkAiringAlarms(showNSFW);
            checkRelatedAiringAlarms(showNSFW);

            // write run time to prefs
            appendRunInfo(DateHelper.getLocalTime().toString() + " success");

            return Result.success();
        } catch (Exception e) {
            // idk, retry on error
            e.printStackTrace();

            // write error info to prefs instead of last run date
            StringWriter b = new StringWriter();
            e.printStackTrace(new PrintWriter(b));
            appendRunInfo(DateHelper.getLocalTime().toString() + " failed (" + e.toString() + ":" + b.toString() + ")");

            // send a notification with the error
            Notification n = notifyManager.notificationBuilder(TenshiNotificationChannel.Default)
                    .setContentTitle("MediaUpdateNotificationsWorker exception")
                    .setContentText(e.toString() + ": " + b.toString())
                    .build();
            notifyManager.sendNow(n);

            return Result.retry();
        }
    }

    private void appendRunInfo(String s) {
        List<String> ri = TenshiPrefs.getObject(TenshiPrefs.Key.DBG_MediaUpdateNotificationsWorkerLastRunInfo, List.class, new ArrayList<String>());
        int toRemove = ri.size() - 9;
        Iterator<String> i = ri.iterator();
        while (toRemove > 0 && i.hasNext()) {
            i.next();
            i.remove();
            toRemove--;
        }

        ri.add(s);
        TenshiPrefs.setObject(TenshiPrefs.Key.DBG_MediaUpdateNotificationsWorkerLastRunInfo, ri);
    }

    /**
     * check and schedule notifications for anime that will air soon
     *
     * @param showNSFW include NSFW anime?
     */
    private void checkAiringAlarms(boolean showNSFW) {
        // get all anime we want to check if they are airing soon
        final List<UserLibraryEntry> animeForAiringCheck = getEntries(db, getCategoriesForAiringAlarms(), showNSFW);

        // get the current time and weekday in japan
        final ZonedDateTime now = DateHelper.getJapanTime();
        final LocalDate nowDate = now.toLocalDate();

        // check each entry
        for (UserLibraryEntry a : animeForAiringCheck) {
            final Anime anime = a.anime;

            // check if anime broadcast info is valid
            // and anime is currently airing
            if (isNull(anime.broadcastStatus)
                    || !anime.broadcastStatus.equals(BroadcastStatus.CurrentlyAiring)
                    || isNull(anime.broadcastInfo)
                    || isNull(anime.broadcastInfo.startTime)
                    || isNull(anime.broadcastInfo.weekday))
                continue;

            // check if anime start date is set and not within the next 7 days
            if (notNull(anime.startDate)
                    && nowDate.until(anime.startDate, ChronoUnit.DAYS) > 7)
                continue;

            // check if anime end date is set and in the past
            // (anime already ended)
            if (notNull(anime.endDate)
                    && nowDate.until(anime.endDate, ChronoUnit.DAYS) < 0)
                continue;


            //TODO overwrite broadcast schedule for Higehiro to be on the current weekday
            // as I keep missing the notification for testing
            if (anime.animeId == 40938) {
                anime.broadcastInfo.weekday = DateHelper.convertDayOfWeek(now.getDayOfWeek());
                //anime.broadcastInfo.startTime = now.toLocalTime().plusMinutes(5);
                Log.w("Tenshi", "MediaUpdateNotification overwrite for 40938 / Higehiro: air on " + anime.broadcastInfo.weekday.name() + " at " + anime.broadcastInfo.startTime.toString());
            }


            // get the next broadcast day and time
            final LocalDateTime nextBroadcast = getNextBroadcast(now, anime.broadcastInfo);

            // check if less than 2h but not in the past
            final long untilNextBroadcast = now.until(nextBroadcast, ChronoUnit.MINUTES);
            if (untilNextBroadcast >= 0 && untilNextBroadcast <= 180) {
                // airs soon, schedule notification for then
                // check how long ago the start date was.
                // if it's in less than a week (< 7) and not in the past (>= 0), assume this is the premiere of the anime
                boolean isPremiere = false;
                if (notNull(anime.startDate)) {
                    final long timeSinceStart = nowDate.until(anime.startDate, ChronoUnit.DAYS);
                    isPremiere = timeSinceStart >= 0 && timeSinceStart < 7;
                }

                // send notification
                sendNotificationForAnimeBroadcastingSoon(a, nextBroadcast, isPremiere);
            }
        }
    }

    /**
     * check and schedule notifications for related anime that will premiere soon
     *
     * @param showNSFW include NSFW anime?
     */
    private void checkRelatedAiringAlarms(boolean showNSFW) {
        // get all anime we want to check if any of their related anime will premiere soon
        final List<UserLibraryEntry> animeForAiringCheck = getEntries(db, getCategoriesForRelatedPremiereAlarms(), showNSFW);

        // get the current time and weekday in japan
        final ZonedDateTime now = DateHelper.getJapanTime();
        final LocalDate nowDate = now.toLocalDate();

        // check each entry's related anime
        for (UserLibraryEntry parent : animeForAiringCheck)
            if (notNull(parent.anime.relatedAnime))
                for (RelatedMedia related : parent.anime.relatedAnime) {
                    final Anime anime = related.relatedAnime;

                    // check if anime broadcast info is valid
                    // and anime is not yet aired
                    if (isNull(anime.broadcastStatus)
                            || !anime.broadcastStatus.equals(BroadcastStatus.NotYetAired)
                            || isNull(anime.broadcastInfo)
                            || isNull(anime.broadcastInfo.startTime)
                            || isNull(anime.broadcastInfo.weekday)
                            || isNull(anime.startDate))
                        continue;

                    // check if anime end date is set and in the past
                    // (anime already ended)
                    if (notNull(anime.endDate)
                            && nowDate.until(anime.endDate, ChronoUnit.DAYS) < 0)
                        continue;

                    // check if less than 1 week until the start date
                    if (nowDate.until(anime.startDate, ChronoUnit.DAYS) > 7)
                        continue;

                    // this anime premiers within the next 7 days,
                    // send notification
                    sendNotificationForRelatedAnimePremiere(parent, related, anime.broadcastInfo);
                }
    }

    /**
     * send a notification that a anime premieres soon (1st episode)
     *
     * @param entry             the anime that airs soon
     * @param nextBroadcastTime when will the anime air
     * @param isPremiere        is this the anime's premiere (1st episode). This threats second+ seasons as if they were separate anime (like MAL does)
     */
    private void sendNotificationForAnimeBroadcastingSoon(@NonNull UserLibraryEntry entry, @NonNull LocalDateTime nextBroadcastTime, boolean isPremiere) {
        //TODO notification channel and content + setWhen

        // create notification
        final Notification notification;
        if (isPremiere) {
            notification = notifyManager.notificationBuilder(TenshiNotificationChannel.Default)
                    .setContentTitle(entry.anime.title + " premiers soon")
                    .setContentText(entry.anime.title + " will premiere soon! check it out now.")
                    .setContentIntent(getDetailsOpenIntent(entry.anime.animeId))
                    .build();
        } else {
            notification = notifyManager.notificationBuilder(TenshiNotificationChannel.Default)
                    .setContentTitle(entry.anime.title + " airs soon")
                    .setContentText(entry.anime.title + " will air soon! check it out now.")
                    .setContentIntent(getDetailsOpenIntent(entry.anime.animeId))
                    .build();
        }

        // schedule notification
        //TODO: just hardcoding to JP timezone right now, but should probably use ZonedDateTime in DateHelper and everywhere else
        notifyManager.sendAt(entry.anime.animeId, notification, nextBroadcastTime.atZone(ZoneId.of("Asia/Tokyo")));
    }

    /**
     * send a notification that a anime related to another anime premieres soon (1st episode)
     *
     * @param parent    the parent anime
     * @param related   the related anime
     * @param broadcast the broadcast info of the related anime
     */
    private void sendNotificationForRelatedAnimePremiere(@NonNull UserLibraryEntry parent, @NonNull RelatedMedia related, @NonNull BroadcastInfo broadcast) {
        //TODO notification channel and content + setWhen

        // create notification
        final Notification notification = notifyManager.notificationBuilder(TenshiNotificationChannel.Default)
                .setContentTitle(related.relatedAnime.title + " will air soon")
                .setContentText(related.relatedAnime.title + "(related to " + parent.anime.title + ") will air on " + broadcast.weekday + "! check it out now.")
                .setContentIntent(getDetailsOpenIntent(related.relatedAnime.animeId))
                .build();

        // and schedule it
        notifyManager.sendNow(related.relatedAnime.animeId, notification);
    }

    /**
     * creates a pending intent to open the details page of a anime
     *
     * @param animeId the anime ID
     * @return the pending intent for opening the details page
     */
    @NonNull
    private PendingIntent getDetailsOpenIntent(int animeId) {
        // create intent to open details
        final Intent detailsIntent = new Intent(getApplicationContext(), AnimeDetailsActivity.class);
        detailsIntent.putExtra(AnimeDetailsActivity.EXTRA_ANIME_ID, animeId);

        // create pending intent
        return PendingIntent.getActivity(getApplicationContext(), animeId, detailsIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * get the next scheduled broadcast date and time
     *
     * @param now           the current time
     * @param broadcastInfo the broadcast schedule
     * @return the next broadcast
     */
    @NonNull
    private LocalDateTime getNextBroadcast(@NonNull ZonedDateTime now, @NonNull BroadcastInfo broadcastInfo) {
        // make datetime with the right time on the current day
        LocalDateTime nextBroadcast = LocalDateTime.of(now.toLocalDate(), broadcastInfo.startTime);

        // increment current date until we find a date with the right weekday
        while (!DateHelper.convertDayOfWeek(nextBroadcast.getDayOfWeek()).equals(broadcastInfo.weekday))
            nextBroadcast = nextBroadcast.plusDays(1);

        // this should be the next scheduled broadcast
        return nextBroadcast;
    }

    /**
     * load all anime with the given status from the database
     *
     * @param db       the database instance
     * @param statuses the statuses to load
     * @param nsfw     include NSFW entries?
     * @return the list of all loaded entries
     */
    @NonNull
    private List<UserLibraryEntry> getEntries(@NonNull TenshiDB db, @NonNull List<LibraryEntryStatus> statuses, boolean nsfw) {
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
     * @return a list of all categories we should check for currently airing anime
     */
    @NonNull
    private List<LibraryEntryStatus> getCategoriesForAiringAlarms() {
        //TODO prefs
        return listOf(LibraryEntryStatus.Watching, LibraryEntryStatus.PlanToWatch);
    }

    /**
     * @return a list of all categories we should check for soon premiering related anime
     */
    @NonNull
    private List<LibraryEntryStatus> getCategoriesForRelatedPremiereAlarms() {
        //TODO prefs
        return listOf(LibraryEntryStatus.Watching, LibraryEntryStatus.PlanToWatch, LibraryEntryStatus.Completed);
    }

    /**
     * should this worker be enabled?
     *
     * @param ctx the context to work in
     * @return should we enable this worker?
     */
    private static boolean isDisabled(@NonNull Context ctx) {
        //TODO conditions by categories
        return false;
    }

    /**
     * get the constrains that are placed on the execution of this worker
     *
     * @return the constrains
     */
    private static Constraints getConstrains() {
        //TODO constrains configuration in props
        return new Constraints.Builder()
                //.setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build();
    }
}
