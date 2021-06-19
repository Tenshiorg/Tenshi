package io.github.shadow578.tenshi.notifications.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;

/**
 * worker for updating the database entries
 */
public class DBUpdateWorker extends WorkerBase {
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
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
    }

    /**
     * @param ctx the context to run in
     * @return should this worker in in the given context?
     */
    public static boolean shouldEnable(@NonNull Context ctx) {
        return AiringAnimeWorker.shouldEnable(ctx) || RelatedAnimeWorker.shouldEnable(ctx);
    }

    /**
     * @param ctx context to run in
     * @return a list of all categories to check in this worker
     */
    @NonNull
    public static List<LibraryEntryStatus> getCategories(@NonNull Context ctx) {
        final ArrayList<LibraryEntryStatus> categories = new ArrayList<>();
        if (AiringAnimeWorker.shouldEnable(ctx))
            categories.addAll(AiringAnimeWorker.getCategories(ctx));

        if (RelatedAnimeWorker.shouldEnable(ctx))
            categories.addAll(RelatedAnimeWorker.getCategories(ctx));

        return categories;
    }

    public DBUpdateWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
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
        //TODO actually update categories
        return Result.success();
    }

    /**
     * @return should this worker run?
     */
    @Override
    protected boolean shouldRun() {
        return shouldEnable(getApplicationContext());
    }
}
