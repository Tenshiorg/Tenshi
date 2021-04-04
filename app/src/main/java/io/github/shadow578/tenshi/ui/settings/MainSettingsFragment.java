package io.github.shadow578.tenshi.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import io.github.shadow578.tenshi.BuildConfig;
import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.db.TenshiDB;
import io.github.shadow578.tenshi.extensionslib.content.ContentAdapterWrapper;
import io.github.shadow578.tenshi.ui.MainActivity;
import io.github.shadow578.tenshi.util.EnumHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.collect;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.join;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.str;
import static io.github.shadow578.tenshi.util.TenshiPrefs.Key;
import static io.github.shadow578.tenshi.util.TenshiPrefs.Theme;
import static io.github.shadow578.tenshi.util.TenshiPrefs.doesKeyExist;
import static io.github.shadow578.tenshi.util.TenshiPrefs.getKeyName;
import static io.github.shadow578.tenshi.util.TenshiPrefs.setBool;

public class MainSettingsFragment extends PreferenceFragmentCompat {
    private int hiddenMenuClicks = 0;

    private static final int REQUEST_CHOOSE_DB_EXPORT_PATH = 21;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // export database to file
        if (requestCode == REQUEST_CHOOSE_DB_EXPORT_PATH
                && resultCode == Activity.RESULT_OK
                && notNull(data.getData())) {
            // open output and database file and start copy
            try (final FileInputStream dbIn = new FileInputStream(TenshiDB.getDatabasePath(requireContext()));
                 final OutputStream out = requireContext().getContentResolver().openOutputStream(data.getData())) {
                // copy file
                final byte[] buf = new byte[1024];
                int r;
                while ((r = dbIn.read(buf)) > 0)
                    out.write(buf, 0, r);
            } catch (IOException e) {
                Toast.makeText(requireContext(), "Failed to export Database!", Toast.LENGTH_SHORT).show();
                Log.e("Tenshi", "error exporting db file: " + e.toString());
                e.printStackTrace();
            }

            // tell user we are done
            Toast.makeText(requireContext(), "Exported Database", Toast.LENGTH_SHORT).show();
        }
    }

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

        // populate and setup export database button
        final Preference exportDb = findPreference("export_database");
        final String dbPath = TenshiDB.getDatabasePath(requireContext()).getAbsolutePath();
        exportDb.setSummary(dbPath);
        exportDb.setOnPreferenceClickListener(preference -> {
            // print path to log
            Log.e("Tenshi", "Database Path: " + dbPath);

            // start document chooser
            final Intent exportIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            exportIntent.setType("*/*");
            startActivityForResult(exportIntent, REQUEST_CHOOSE_DB_EXPORT_PATH);
            return true;
        });

        // DEBUG: readonly preference for all keys
        setupDebugPrefs("dbg_prefs_category");

        // DEBUG: list all found content adapters
        setupContentAdapters("dbg_ca_category");
    }

    private void setupDebugPrefs(@SuppressWarnings("SameParameterValue") String category) {
        // dump all prefs in Key enum
        final ArrayList<String> dumpedKeys = new ArrayList<>();
        final PreferenceCategory cat = findPreference(category);
        for (Key key : Key.values()) {
            // create preference
            Preference pref = new Preference(requireContext());
            pref.setTitle(key.name());
            pref.setSummary(getAny(key, "-"));
            dumpedKeys.add(TenshiPrefs.getKeyName(key));

            // add preference to category
            cat.addPreference(pref);
        }

        // dump any key that is not in Keys enum (with extended key)
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext());
        final Map<String, ?> allPrefs = prefs.getAll();
        for (String key : allPrefs.keySet()) {
            // skip if already dumped this key
            if (dumpedKeys.contains(key))
                continue;

            // get value
            String value = allPrefs.get(key).toString();

            // create preference
            Preference pref = new Preference(requireContext());
            pref.setTitle(key);
            pref.setSummary(value);

            // add preference
            cat.addPreference(pref);
        }
    }

    private void setupContentAdapters(@SuppressWarnings("SameParameterValue") String category) {
        // wait until discovery finsihed
        TenshiApp.getContentAdapterManager().addOnDiscoveryEndCallback(manager -> {
            final PreferenceCategory cat = findPreference(category);
            for (ContentAdapterWrapper ca : manager.getAdapters()) {
                // create preference
                Preference pref = new Preference(requireContext());
                pref.setTitle(ca.getDisplayName());
                pref.setSummary(fmt("%s (API %d)", ca.getUniqueName(), ca.getApiVersion()));

                // add preference to category
                cat.addPreference(pref);
            }
        });
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
