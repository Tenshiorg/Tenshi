package io.github.shadow578.tenshi.notifications.workers;

import android.app.Notification;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.BroadcastInfo;
import io.github.shadow578.tenshi.mal.model.UserLibraryEntry;
import io.github.shadow578.tenshi.mal.model.type.BroadcastStatus;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.notifications.TenshiNotificationChannel;
import io.github.shadow578.tenshi.notifications.db.SentNotificationInfo;
import io.github.shadow578.tenshi.util.DateHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.listOf;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * notification worker for currently airing anime updates
 */
public class AiringAnimeWorker extends WorkerBase {
    /**
     * get the constrains that are placed on the execution of this worker
     *
     * @param ctx context to work in
     * @return the constrains
     */
    @NonNull
    public static Constraints getConstrains(@NonNull Context ctx) {
        //TODO constrains configuration in props
        return new Constraints.Builder()
                //.setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
    }

    /**
     * @param ctx the context to run in
     * @return should this worker in in the given context?
     */
    public static boolean shouldEnable(@NonNull Context ctx) {
        //TODO load from prefs
        return true;
    }

    /**
     * @param ctx context to run in
     * @return a list of all categories to check in this worker
     */
    @NonNull
    public static List<LibraryEntryStatus> getCategories(@NonNull Context ctx) {
        //TODO load from prefs
        return listOf(LibraryEntryStatus.Watching, LibraryEntryStatus.PlanToWatch);
    }

    public AiringAnimeWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * wrapped function for {@link Worker#doWork()}.
     * only runs if {@link #shouldRun()} is true
     *
     * @return work result
     */
    @Override
    protected Result run() {
        try {
            // run the checks
            checkAiring();

            //TODO dev testing
            // write run time to prefs
            appendRunInfo(DateHelper.getLocalTime().toString() + " success");
            return Result.success();
        } catch (Exception e) {
            // idk, retry on error
            e.printStackTrace();

            //TODO dev testing
            // write error info to prefs instead of last run date
            StringWriter b = new StringWriter();
            e.printStackTrace(new PrintWriter(b));
            appendRunInfo(DateHelper.getLocalTime().toString() + " failed (" + e.toString() + ":" + b.toString() + ")");

            // send a notification with the error
            Notification n = getNotifyManager().notificationBuilder(TenshiNotificationChannel.Default)
                    .setContentTitle("MediaUpdateNotificationsWorker exception")
                    .setContentText(e.toString() + ": " + b.toString())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(e.toString() + ": " + b.toString()))
                    .build();
            getNotifyManager().sendNow(n);


            return Result.retry();
        }
    }

    /**
     * check all anime with library category {@link #getCategories(Context)} that are currently in the db for
     * if they air soon
     */
    private void checkAiring() {
        //get nsfw preference
        requirePrefs();
        final boolean showNSFW = TenshiPrefs.getBool(TenshiPrefs.Key.NSFW, false);

        // get all anime we want to check if they are airing soon
        final List<UserLibraryEntry> animeForAiringCheck = getEntries(getDB(), getCategories(getApplicationContext()), showNSFW);

        // get the current time and weekday in japan
        final ZonedDateTime now = DateHelper.getJapanTime();
        final LocalDate nowDate = now.toLocalDate();

        // check each entry
        for (UserLibraryEntry a : animeForAiringCheck) {
            final Anime anime = a.anime;

            //TODO overwrite broadcast schedule for Higehiro to be on the current weekday
            // as I keep missing the notification for testing
            if (anime.animeId == 40938) {
                anime.broadcastInfo = new BroadcastInfo();
                anime.broadcastInfo.weekday = DateHelper.convertDayOfWeek(now.getDayOfWeek());
                anime.broadcastInfo.startTime = now.toLocalTime().plusMinutes(5);
                anime.endDate = LocalDate.of(2022, 12, 24);
                anime.broadcastStatus = BroadcastStatus.CurrentlyAiring;
                Log.w("Tenshi", "MediaUpdateNotification overwrite for 40938 / Higehiro: air on " + anime.broadcastInfo.weekday.name() + " at " + anime.broadcastInfo.startTime.toString());
            }

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

            // get the next broadcast day and time
            final ZonedDateTime nextBroadcast = anime.broadcastInfo.getNextBroadcast(now);

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
                sendNotificationFor(a, nextBroadcast, isPremiere);
            }
        }
    }

    /**
     * send a notification for a airing anime
     *
     * @param libraryEntry  the anime library entry to send the notification for
     * @param nextBroadcast the next scheduled broadcast time of the anime
     * @param isPremiere    is this broadcast the first broadcast?
     */
    private void sendNotificationFor(@NonNull UserLibraryEntry libraryEntry, @NonNull ZonedDateTime nextBroadcast, boolean isPremiere) {
        // get anime from entry
        final Anime a = libraryEntry.anime;

        // get notification content
        final String title, text;
        if (isPremiere) {
            title = "Upcoming Anime premiere";
            text = fmt("%s will premiere soon!", a.title);

        } else {
            title = "Anime will air soon";
            text = fmt("%s will air soon!", a.title);
        }

        // check if already in db, do not send if it is
        // otherwise insert
        if (!getNotifyDB().notificationsDB().insertIfNotPresent(SentNotificationInfo.create(Duration.ofDays(7),
                a.animeId,
                true,
                title,
                text,
                TenshiNotificationChannel.Default.id(),
                "at: " + nextBroadcast.toString()))) {
            // sent this notification already, do not sent again
            return;
        }

        // create notification
        //TODO channel and content hardcode
        final Notification notification = getNotifyManager().notificationBuilder(TenshiNotificationChannel.Default)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setContentIntent(getDetailsOpenIntent(a.animeId))
                .setAutoCancel(true)
                .build();

        // schedule the notification
        getNotifyManager().sendAt(a.animeId, notification, nextBroadcast);
    }

    /**
     * @return should this worker run?
     */
    @Override
    protected boolean shouldRun() {
        return shouldEnable(getApplicationContext());
    }

    /**
     * TODO testing, remove asap
     */
    private void appendRunInfo(String s) {
        List<String> ri = TenshiPrefs.getObject(TenshiPrefs.Key.DEV_AiringAnimeWorkerLog, List.class, new ArrayList<String>());
        int toRemove = ri.size() - 9;
        Iterator<String> i = ri.iterator();
        while (toRemove > 0 && i.hasNext()) {
            i.next();
            i.remove();
            toRemove--;
        }

        ri.add(s);
        TenshiPrefs.setObject(TenshiPrefs.Key.DEV_AiringAnimeWorkerLog, ri);
    }
}
