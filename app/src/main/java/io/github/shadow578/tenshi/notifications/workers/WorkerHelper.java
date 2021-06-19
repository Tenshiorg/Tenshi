package io.github.shadow578.tenshi.notifications.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * class to manage registration of (notification) workers
 */
public final class WorkerHelper {

    /**
     * unique name for the notification worker(s)
     */
    private static final String NOTIFICATIONS_WORKER_UNIQUE_NAME = "io.github.shadow578.tenshi.NOTIFICATIONS_WORKER";

    /**
     * register the notification workers
     * @param ctx the context to work in
     */
    public static void registerNotificationWorkers(@NonNull Context ctx) {
        // create the workers
        final OneTimeWorkRequest dbUpdate = createDBUpdate(ctx);
        final OneTimeWorkRequest airingAnime = createAiringAnime(ctx);
        final OneTimeWorkRequest relatedAnime = createRelatedAnime(ctx);

        // make the reschedule task wait 1h before firing, delaying the re- run of the tasks by that time
        final OneTimeWorkRequest reschedule = new OneTimeWorkRequest.Builder(RescheduleWorker.class)
                .setInitialDelay(1, TimeUnit.HOURS)
                .build();

        // do not enqueue anything if both airingAnime and relatedAnime workers are null
        if (isNull(airingAnime) && isNull(relatedAnime))
            return;

        // create list of all notification workers
        final List<OneTimeWorkRequest> notifyWorkers = new ArrayList<>();
        if (notNull(airingAnime))
            notifyWorkers.add(airingAnime);
        if (notNull(relatedAnime))
            notifyWorkers.add(relatedAnime);

        // enqueue the work
        if (notNull(dbUpdate)) {
            WorkManager.getInstance(ctx)
                    .beginUniqueWork(NOTIFICATIONS_WORKER_UNIQUE_NAME, ExistingWorkPolicy.REPLACE, dbUpdate)
                    .then(notifyWorkers)
                    .then(reschedule)
                    .enqueue();
        } else {
            WorkManager.getInstance(ctx)
                    .beginUniqueWork(NOTIFICATIONS_WORKER_UNIQUE_NAME, ExistingWorkPolicy.REPLACE, notifyWorkers)
                    .then(reschedule)
                    .enqueue();
        }
    }

    /**
     * create the work request for {@link DBUpdateWorker}
     *
     * @param ctx the context to create in
     * @return the work request. null if this worker should not run
     */
    @Nullable
    private static OneTimeWorkRequest createDBUpdate(@NonNull Context ctx) {
        if (DBUpdateWorker.shouldEnable(ctx))
            return new OneTimeWorkRequest.Builder(DBUpdateWorker.class)
                    .setConstraints(DBUpdateWorker.getConstrains(ctx))
                    .build();

        return null;
    }

    /**
     * create the work request for {@link AiringAnimeWorker}
     *
     * @param ctx the context to create in
     * @return the work request. null if this worker should not run
     */
    @Nullable
    private static OneTimeWorkRequest createAiringAnime(@NonNull Context ctx) {
        if (AiringAnimeWorker.shouldEnable(ctx))
            return new OneTimeWorkRequest.Builder(AiringAnimeWorker.class)
                    .setConstraints(AiringAnimeWorker.getConstrains(ctx))
                    .build();

        return null;
    }

    /**
     * create the work request for {@link RelatedAnimeWorker}
     *
     * @param ctx the context to create in
     * @return the work request. null if this worker should not run
     */
    @Nullable
    private static OneTimeWorkRequest createRelatedAnime(@NonNull Context ctx) {
        if (RelatedAnimeWorker.shouldEnable(ctx))
            return new OneTimeWorkRequest.Builder(RelatedAnimeWorker.class)
                    .setConstraints(RelatedAnimeWorker.getConstrains(ctx))
                    .build();

        return null;
    }

    /**
     * simple worker that only calls {@link #registerNotificationWorkers(Context)}
     */
    public static class RescheduleWorker extends Worker {
        public RescheduleWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NotNull
        @Override
        public Result doWork() {
            // reschedule all workers again
            registerNotificationWorkers(getApplicationContext());
            return Result.success();
        }
    }
}
