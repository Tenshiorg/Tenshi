package io.github.shadow578.tenshi.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationManagerCompat;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.extensionslib.lang.BiFunction;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrWhitespace;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.withRet;

/**
 * Tenshi notification channels registry and management.
 * <p>
 * all channels registered should be registered on app start using {@link #registerAll(Context, BiFunction)}, so
 * they don't have to be initialized before sending a notification
 */
public enum TenshiNotificationChannel {
    /**
     * default notification channel.
     * <p>
     * only for use when testing stuff (and the actual channel is not setup yet) or for notifications that are normally not shown
     */
    Default("io.github.shadow578.tenshi.notifications.DEFAULT",
            R.string.notify_channel_default_name,
            R.string.notify_channel_default_desc);

// region boring background stuff
    /**
     * notification channel ID
     */
    @NonNull
    private final String id;

    /**
     * display name of this channel. stringRes
     */
    @Nullable
    @StringRes
    private final Integer nameRes;

    /**
     * display description of this channel. stringRes
     */
    @Nullable
    @StringRes
    private final Integer descRes;

    /**
     * define a new notification channel. the channel will have no description and a fallback name
     *
     * @param id the channel ID
     */
    @SuppressWarnings("unused")
    TenshiNotificationChannel(@NonNull String id) {
        this.id = id;
        nameRes = null;
        descRes = null;
    }

    /**
     * define a new notification channel. the channel will have no description
     *
     * @param id   the channel ID
     * @param name the display name of the channel
     */
    @SuppressWarnings("unused")
    TenshiNotificationChannel(@NonNull String id, @StringRes int name) {
        this.id = id;
        nameRes = name;
        descRes = null;
    }

    /**
     * define a new notification channel
     *
     * @param id   the channel ID
     * @param name the display name of the channel
     * @param desc the display description of the channel
     */
    TenshiNotificationChannel(@NonNull String id, @StringRes int name, @StringRes int desc) {
        this.id = id;
        nameRes = name;
        descRes = desc;
    }

    /**
     * @return id of this channel definition
     */
    @NonNull
    @Override
    public String toString() {
        return id();
    }

    /**
     * @return id of this channel definition
     */
    @NonNull
    public String id() {
        return id;
    }

    /**
     * create the notification channel from the definition
     *
     * @param ctx the context to resolve strings in
     * @return the channel, with id, name, desc and importance set
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    private NotificationChannel createChannel(@NonNull Context ctx) {
        // get name and description
        final String chId = id();
        final String chName = withRet(nameRes, chId, ctx::getString);
        final String chDesc = withRet(descRes, "", ctx::getString);

        // create the channel
        final NotificationChannel ch = new NotificationChannel(chId, chName, NotificationManager.IMPORTANCE_DEFAULT);
        if (!nullOrWhitespace(chDesc))
            ch.setDescription(chDesc);
        return ch;
    }

    /**
     * register all notification channels
     *
     * @param ctx     the context to register in
     * @param cfgFunc channel configuration function. while the display name and description can be handled by the channel definition itself, this function
     *                allows for changes to the notification channels before they are registered, aswell as disabling channels completely.<br/>
     *                signature of the function:
     *                <p>
     *                boolean cfgFunc({@link TenshiNotificationChannel} value, {@link NotificationChannel} channel);
     *                <p>
     *                where the return value indicates if the channel should be registered.<br/>
     *                if set to null, all channels are registered with default values
     */
    public static void registerAll(@NonNull Context ctx, @Nullable BiFunction<Boolean, TenshiNotificationChannel, NotificationChannel> cfgFunc) {
        // check SDK level
        // notification channels are only supported on API 26 and up
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        // get notification manager
        final NotificationManagerCompat notifyMgr = NotificationManagerCompat.from(ctx);

        // register channels
        for (TenshiNotificationChannel ch : TenshiNotificationChannel.values()) {
            // create the channel and pass to configure function
            final NotificationChannel channel = ch.createChannel(ctx);
            if (isNull(cfgFunc) || cfgFunc.invoke(ch, channel)) {
                // configure is OK or skipped, register the channel
                notifyMgr.createNotificationChannel(channel);
            }
        }
    }
    //endregion
}
