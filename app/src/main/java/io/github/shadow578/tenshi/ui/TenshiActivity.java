package io.github.shadow578.tenshi.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.MaterialColors;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.lang.LanguageUtils.nullOrEmpty;

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
}
