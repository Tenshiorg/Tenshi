package io.github.shadow578.tenshi.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.ui.TenshiActivity;

/**
 * the settings activity
 */
public class SettingsActivity extends TenshiActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // load main fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_fragment_container, new MainSettingsFragment())
                .commit();
    }
}
