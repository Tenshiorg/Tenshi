package io.github.shadow578.tenshi.ui.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.ui.TenshiActivity;

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

        // add info that this is WIP
        Toast.makeText(this, "Settings are still WIP", Toast.LENGTH_SHORT).show();
    }
}
