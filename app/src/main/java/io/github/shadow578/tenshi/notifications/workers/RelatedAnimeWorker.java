package io.github.shadow578.tenshi.notifications.workers;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.RelatedMedia;
import io.github.shadow578.tenshi.mal.model.UserLibraryEntry;
import io.github.shadow578.tenshi.mal.model.type.BroadcastStatus;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.notifications.TenshiNotificationChannel;
import io.github.shadow578.tenshi.util.DateHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.listOf;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * notification worker for related airing anime updates
 */
public class RelatedAnimeWorker extends WorkerBase {
    /**
     * get the constrains that are placed on the execution of this worker
     *
     * @param ctx  context to work in
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
        return listOf(LibraryEntryStatus.Watching, LibraryEntryStatus.PlanToWatch, LibraryEntryStatus.Completed);
    }

    public RelatedAnimeWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
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
            checkRelated();

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
                    .build();
            getNotifyManager().sendNow(n);

            return Result.retry();
        }
    }

    /**
     * check all anime with a library category of {@link #getCategories(Context)} for new
     * related anime that air soon
     */
    private void checkRelated() {
        //get nsfw preference
        requirePrefs();
        final boolean showNSFW = TenshiPrefs.getBool(TenshiPrefs.Key.NSFW, false);

        // get all anime we want to check if any of their related anime will premiere soon
        final List<UserLibraryEntry> animeForAiringCheck = getEntries(getDB(), getCategories(getApplicationContext()), showNSFW);

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
                    sendNotification(parent, related);
                }
    }

    /**
     * send a notification for a soon to air related anime
     *
     * @param parentEntry  the parent anime of the related anime
     * @param relatedMedia the related anime that will soon air
     */
    private void sendNotification(@NonNull UserLibraryEntry parentEntry, @NonNull RelatedMedia relatedMedia) {
        // get anime
        final Anime a = relatedMedia.relatedAnime;

        // check broadcast info not null (never is, but needed to make AS shut up)
        if (isNull(a.broadcastInfo))
            return;

        // create notification
        //TODO channel and content hardcode
        final Notification notification = getNotifyManager().notificationBuilder(TenshiNotificationChannel.Default)
                .setContentTitle(a.title + " will air soon")
                .setContentText(a.title + "(related to " + parentEntry.anime.title + ") will air on " + a.broadcastInfo.weekday + "! check it out now.")
                .setContentIntent(getDetailsOpenIntent(a.animeId))
                .build();

        // and schedule it
        getNotifyManager().sendNow(a.animeId, notification);
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
        List<String> ri = TenshiPrefs.getObject(TenshiPrefs.Key.DEV_RelatedAnimeWorkerLog, List.class, new ArrayList<String>());
        int toRemove = ri.size() - 9;
        Iterator<String> i = ri.iterator();
        while (toRemove > 0 && i.hasNext()) {
            i.next();
            i.remove();
            toRemove--;
        }

        ri.add(s);
        TenshiPrefs.setObject(TenshiPrefs.Key.DEV_RelatedAnimeWorkerLog, ri);
    }
}
