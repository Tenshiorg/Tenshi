package io.github.shadow578.tenshi.util;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import io.github.shadow578.tenshi.R;

/**
 * helper class for CustomTabsIntent creation
 */
public class CustomTabsHelper {
    /**
     * open a link in a chrome custom- tab
     *
     * @param ctx       the context to open from
     * @param targetUrl the url to open
     * @return the intent that was created and launched
     */
    @SuppressWarnings("UnusedReturnValue")
    @NonNull
    public static CustomTabsIntent openInCustomTab(@NonNull Context ctx, @NonNull String targetUrl) {
        CustomTabsIntent tabIntent = new CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(new CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(ContextCompat.getColor(ctx, R.color.primary))
                        .build())
                .build();
        tabIntent.launchUrl(ctx, Uri.parse(targetUrl));

        return tabIntent;
    }
}
