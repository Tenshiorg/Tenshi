package io.github.shadow578.tenshi.ui.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import io.github.shadow578.tenshi.BuildConfig;
import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.ui.MainActivity;
import io.github.shadow578.tenshi.util.EnumHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.util.TenshiPrefs.*;
import static io.github.shadow578.tenshi.lang.LanguageUtils.*;

public class MainSettingsFragment extends PreferenceFragmentCompat {
    private int hiddenMenuClicks = 0;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_main_screen, rootKey);

        // populate themes
        final ListPreference themePref = findPreference(getKeyName(Key.Theme));
        final String[] themeValues = collect(Theme.values(), Enum::name).toArray(new String[0]);
        themePref.setEntries(themeValues);
        themePref.setEntryValues(themeValues);
        themePref.setDefaultValue(Theme.FollowSystem);

        // populate startup sections
        final ListPreference tabPref = findPreference(getKeyName(Key.StartupSection));
        final String[] tabValues = collect(MainActivity.Section.values(), Enum::name).toArray(new String[0]);
        tabPref.setEntries(tabValues);
        tabPref.setEntryValues(tabValues);
        tabPref.setDefaultValue(EnumHelper.valueOf(MainActivity.Section.Home));

        // add listener to logout button
        final Preference logoutBtn = findPreference("logout_btn");
        logoutBtn.setOnPreferenceClickListener(preference -> {
            // invalidate the saved token
            Toast.makeText(requireContext(), "Logged Out", Toast.LENGTH_SHORT).show();
            TenshiApp.INSTANCE.invalidateTokenAndLogin(requireActivity());
            return true;
        });

        // populate app version
        final Preference appVersion = findPreference("app_version");
        appVersion.setSummary(join(" - ", BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE));
        appVersion.setOnPreferenceClickListener(preference -> {
            hiddenMenuClicks++;
            if (hiddenMenuClicks > 5) {
                setBool(Key.ShowDebugOptions, true);
                Toast.makeText(requireContext(), "🐱", Toast.LENGTH_SHORT).show();
            } else if (hiddenMenuClicks > 4)
                Toast.makeText(requireContext(), "Almost There", Toast.LENGTH_SHORT).show();
            return true;
        });


        // DEBUG: readonly preference for all keys
        setupDebugPrefs("dbg_debug_category");
    }

    private void setupDebugPrefs(@SuppressWarnings("SameParameterValue") String category) {
        final PreferenceCategory cat = findPreference(category);
        for (Key key : Key.values()) {
            // create preference
            Preference pref = new Preference(requireContext());
            pref.setTitle(key.name());
            pref.setSummary(getAny(key, "-"));

            // add preference to category
            cat.addPreference(pref);
        }
    }

    private String getAny(Key key, String def) {
        if (!doesKeyExist(key))
            return def;

        try {
            return TenshiPrefs.getString(key, def);
        } catch (ClassCastException ignored) {
        }

        try {
            return str(TenshiPrefs.getBool(key, false));
        } catch (ClassCastException ignored) {
        }

        try {
            return str(TenshiPrefs.getInt(key, 0));
        } catch (ClassCastException ignored) {
        }

        try {
            return str(TenshiPrefs.getLong(key, 0));
        } catch (ClassCastException ignored) {
        }

        try {
            return str(TenshiPrefs.getFloat(key, 0f));
        } catch (ClassCastException ignored) {
        }

        return def;
    }
}
