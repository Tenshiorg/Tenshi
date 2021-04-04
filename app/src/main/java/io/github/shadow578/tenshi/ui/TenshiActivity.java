package io.github.shadow578.tenshi.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.util.TenshiPrefs;
import io.github.shadow578.tenshi.util.Util;

/**
 * base activity for all activities
 */
public class TenshiActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        TenshiPrefs.init(getApplicationContext());
        applyAmoledTheme();
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }

        getWindow().setStatusBarColor(MaterialColors.getColor(this, R.attr.colorPrimaryDark, Color.WHITE));
    }

    /**
     * load the amoled theme if enabled in prefs
     */
    private void applyAmoledTheme() {
        if (TenshiPrefs.getEnum(TenshiPrefs.Key.Theme, TenshiPrefs.Theme.class, TenshiPrefs.Theme.FollowSystem).equals(TenshiPrefs.Theme.Amoled))
            setTheme(R.style.TenshiTheme_Amoled);
        else
            setTheme(R.style.TenshiTheme);
    }

    /**
     * check if the user is authenticated.
     * if not, close this activity and redirect to LoginActivity
     */
    protected void requireUserAuthenticated() {
        if (!TenshiApp.isUserAuthenticated()) {
            // not logged in, redirect to login activity
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    /**
     * flag to indicate that the "no internet connection" snack bar has been dismissed
     */
    public static boolean noConnectionSnackDismissed = false;

    /**
     * show a snackbar informing the user he is offline.
     *
     * @param anchor the view to anchor the snackbar to.
     */
    protected void showSnackbarIfOffline(@NonNull View anchor) {
        // show snackbar if offline
        if (Util.getConnectionType(this).equals(Util.ConnectionType.None) && !noConnectionSnackDismissed) {
            // resolve error color for current theme
            final TypedValue tvColorError = new TypedValue();
            final TypedValue tvColorOnError = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorError, tvColorError, true);
            getTheme().resolveAttribute(R.attr.colorOnError, tvColorOnError, true);

            // create and show snackbar
            final Snackbar snacc = Snackbar.make(anchor, R.string.shared_snack_no_internet, Snackbar.LENGTH_INDEFINITE);
            snacc.setAction(R.string.shared_snack_no_internet_dismiss, v -> {
                snacc.dismiss();
                noConnectionSnackDismissed = true;
            })
                    .setBackgroundTint(tvColorError.data)
                    .setTextColor(tvColorOnError.data)
                    .setActionTextColor(tvColorOnError.data)
                    .show();
        }
    }
}
