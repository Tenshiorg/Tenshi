package io.github.shadow578.tenshi.notifications.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;

/**
 * boot listener for clearing scheduled notifications from the notifications database on reboots
 */
public class NotificationBootListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        // validate intent action
        if (isNull(intent) || !intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
            return;

        // start service
        final Intent serviceIntent = new Intent(ctx, NotificationBootService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(serviceIntent);
        else
            ctx.startService(serviceIntent);

        Log.i("Tenshi", "NotificationBootListener received ON_BOOT_COMPLETED, service started.");
    }
}
