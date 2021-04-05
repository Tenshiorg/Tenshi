package io.github.shadow578.tenshi.ui.settings;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.db.TenshiDB;
import io.github.shadow578.tenshi.extensionslib.content.ContentAdapterWrapper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.async;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.cast;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.elvisEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.str;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;

/**
 * the developer options preference screen
 */
public class DeveloperSettingsFragment extends PreferenceFragmentCompat {

    /**
     * request id for document chooser used when choosing the database export path
     */
    private static final int REQUEST_CHOOSE_DB_EXPORT_PATH = 21;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // export database to file
        if (requestCode == REQUEST_CHOOSE_DB_EXPORT_PATH
                && resultCode == Activity.RESULT_OK
                && notNull(data)
                && notNull(data.getData()))
            exportDatabaseTo(data.getData());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // load screen from xml resource
        setPreferencesFromResource(R.xml.settings_dev_screen, rootKey);

        // init dynamic stuff
        final Context ctx = requireContext();
        setupThrowExceptionFunctions();
        setupDatabaseFunctions(ctx);
        setupSharedPrefsFunctions(ctx);
        setupContentAdapterFunctions(ctx);
    }

    /**
     * setup the throw exeption buttons
     */
    private void setupThrowExceptionFunctions() {
        // throw in ui thread
        final LongClickablePreference uiPref = findPreference("dbg_exception_in_ui");
        with(uiPref, throwBtn -> throwBtn.setOnPreferenceLongClickListener(preference -> {
            async(() -> "", p -> {
                throw new IllegalStateException("App Crash from Developer Option, UI Thread");
            });
            return true;
        }));

        // throw in background thread
        final LongClickablePreference bgPref = findPreference("dbg_exception_in_async");
        with(bgPref, throwBtn -> throwBtn.setOnPreferenceLongClickListener(preference -> {
            async(() -> {
                throw new IllegalStateException("App Crash from Developer Option, Background Thread");
            });
            return true;
        }));
    }

    /**
     * setup debug functions for database
     *
     * @param ctx the context to work in
     */
    private void setupDatabaseFunctions(@NonNull Context ctx) {
        // setup 'export database' button
        final Preference exportDbPref = findPreference("dbg_export_database");
        final String dbPath = TenshiDB.getDatabasePath(ctx).getAbsolutePath();
        with(exportDbPref, exportDb -> {
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
        });

        // setup 'clean database' button
        final Preference cleanDbPref = findPreference("dgb_cleanup_database");
        with(cleanDbPref,
                cleanDb -> cleanDb.setOnPreferenceClickListener(preference -> {
                    // start cleanup async
                    async(() -> TenshiApp.getDB().cleanupDatabase(),
                            count -> Toast.makeText(ctx, fmt("Cleanup removed %d entries", count), Toast.LENGTH_SHORT).show());
                    return true;
                }));

        // setup 'delete database' button
        final Preference deleteDbPref = findPreference("dbg_delete_database");
        with(deleteDbPref,
                deleteDb -> deleteDb.setOnPreferenceClickListener(preference -> {
                    async(() -> TenshiApp.getDB().clearAllTables());
                    Toast.makeText(ctx, "Deleted Database", Toast.LENGTH_SHORT).show();
                    return true;
                }));
    }

    /**
     * setup debug functions for shared preferences
     *
     * @param ctx the context to work in
     */
    private void setupSharedPrefsFunctions(@NonNull Context ctx) {
        // find preferences container for key enum
        final PreferenceCategory enumPref = findPreference("dbg_prefs_of_enum");
        with(enumPref, container -> {
            // dump all prefs from the Key enum
            // this should normally include all preferences used by tenshi
            for (TenshiPrefs.Key key : TenshiPrefs.Key.values()) {
                // get key name and value
                final String keyName = TenshiPrefs.getKeyName(key);
                final String value = getAny(key, "(empty)");

                // create the preference
                final LongClickablePreference pref = new LongClickablePreference(ctx);
                pref.setTitle(keyName);
                pref.setSummary(value);
                pref.setOnPreferenceClickListener(preference -> {
                    copyToClip(ctx, keyName, value);
                    Toast.makeText(ctx, R.string.shared_toast_copied_to_clip, Toast.LENGTH_SHORT).show();
                    return true;
                });
                pref.setOnPreferenceLongClickListener(preference -> {
                    TenshiPrefs.clear(key);
                    container.removePreference(preference);
                    //Toast.makeText(ctx, "Cleared Preference " + keyName, Toast.LENGTH_SHORT).show();

                    // hide container if empty
                    if (container.getPreferenceCount() <= 0)
                        container.setVisible(false);
                    return true;
                });

                // add the preference to the category
                container.addPreference(pref);
            }
        });

        // find preferences container for all other prefs
        final PreferenceCategory otherPref = findPreference("dbg_prefs_other");
        with(otherPref, container -> {
            // get all keys in the enum
            final ArrayList<String> allEnumKeys = new ArrayList<>();
            for (TenshiPrefs.Key key : TenshiPrefs.Key.values())
                allEnumKeys.add(TenshiPrefs.getKeyName(key));

            // dump all keys that are not in the Key enum
            // this normally should not contain anything now, but once this was required for extended key prefs
            // since i have the code already, why not keep it
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
            final Map<String, ?> allPrefs = prefs.getAll();
            int count = 0;
            for (String key : allPrefs.keySet()) {
                // check if this key is in Key enum
                if (allEnumKeys.contains(key))
                    continue;

                // get the value
                final Object value = allPrefs.get(key);
                if (isNull(value))
                    continue;

                final String valueStr = elvisEmpty(value.toString(), "(empty)");

                // create the preference
                final LongClickablePreference pref = new LongClickablePreference(ctx);
                pref.setTitle(key);
                pref.setSummary(valueStr);
                pref.setOnPreferenceClickListener(preference -> {
                    copyToClip(ctx, key, valueStr);
                    Toast.makeText(ctx, R.string.shared_toast_copied_to_clip, Toast.LENGTH_SHORT).show();
                    return true;
                });
                pref.setOnPreferenceLongClickListener(preference -> {
                    prefs.edit().remove(key).apply();
                    container.removePreference(preference);
                    //Toast.makeText(ctx, "Cleared Preference " + key, Toast.LENGTH_SHORT).show();

                    // hide container if empty
                    if (container.getPreferenceCount() <= 0)
                        container.setVisible(false);
                    return true;
                });

                // add the preference to the category
                container.addPreference(pref);
                count++;
            }

            // hide this category if no children were added
            if (count <= 0)
                container.setVisible(false);
        });
    }

    /**
     * setup debug functions for content adapters
     *
     * @param ctx the context to work in
     */
    private void setupContentAdapterFunctions(@NonNull Context ctx) {
        // find preferences container for key enum
        final PreferenceCategory enumPref = findPreference("dbg_content_adapters_container");
        with(enumPref, container -> {
            // make invisible until loaded
            container.setVisible(false);

            // wait until discovery finished
            TenshiApp.getContentAdapterManager().addOnDiscoveryEndCallback(manager -> {
                // get all content adapters
                int count = 0;
                for (ContentAdapterWrapper ca : manager.getAdapters()) {
                    // get values
                    final String displayName = ca.getDisplayName();
                    final String uniqueNameAndApi = fmt("%s (API %d)", ca.getUniqueName(), ca.getApiVersion());

                    // create the preference
                    final Preference pref = new Preference(ctx);
                    pref.setTitle(displayName);
                    pref.setSummary(uniqueNameAndApi);
                    pref.setOnPreferenceClickListener(preference -> {
                        copyToClip(ctx, displayName, uniqueNameAndApi);
                        Toast.makeText(ctx, R.string.shared_toast_copied_to_clip, Toast.LENGTH_SHORT).show();
                        return true;
                    });

                    // add the preference
                    container.addPreference(pref);
                    count++;
                }

                // hide this category if no children were added
                container.setVisible(count > 0);
            });
        });
    }

    /**
     * export the database to a file
     *
     * @param targetPath the path of the file to export to
     */
    private void exportDatabaseTo(@NonNull Uri targetPath) {
        // open output and database file and start copy
        try (final FileInputStream dbIn = new FileInputStream(TenshiDB.getDatabasePath(requireContext()));
             final OutputStream out = requireContext().getContentResolver().openOutputStream(targetPath)) {
            // copy file
            final byte[] buf = new byte[1024];
            int r;
            while ((r = dbIn.read(buf)) > 0)
                out.write(buf, 0, r);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Database export failed!", Toast.LENGTH_SHORT).show();
            Log.e("Tenshi", "error exporting db file: " + e.toString());
            e.printStackTrace();
        }

        // tell user we are done
        Toast.makeText(requireContext(), "Exported Database to " + targetPath.toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * get a key of any value from tenshi prefs.
     *
     * @param key the key to get
     * @param def the fallback default value
     * @return the value, as string
     */
    @NonNull
    private String getAny(@NonNull TenshiPrefs.Key key, @SuppressWarnings("SameParameterValue") @NonNull String def) {
        if (!TenshiPrefs.doesKeyExist(key))
            return def;

        try {
            return TenshiPrefs.getString(key, def);
        } catch (ClassCastException ignored) {
        }

        try {
            return elvisEmpty(str(TenshiPrefs.getBool(key, false)), def);
        } catch (ClassCastException ignored) {
        }

        try {
            return elvisEmpty(str(TenshiPrefs.getInt(key, 0)), def);
        } catch (ClassCastException ignored) {
        }

        try {
            return elvisEmpty(str(TenshiPrefs.getLong(key, 0)), def);
        } catch (ClassCastException ignored) {
        }

        try {
            return elvisEmpty(str(TenshiPrefs.getFloat(key, 0f)), def);
        } catch (ClassCastException ignored) {
        }

        return def;
    }

    /**
     * copy a message to the clipboard
     *
     * @param ctx   the context to work in
     * @param title the title to use
     * @param text  the text to copy to clipboard
     */
    private void copyToClip(@NonNull Context ctx, @NonNull String title, @NonNull String text) {
        final ClipboardManager clip = cast(ctx.getSystemService(Context.CLIPBOARD_SERVICE));
        final ClipData data = ClipData.newPlainText(title, text);

        if (notNull(clip))
            clip.setPrimaryClip(data);
    }
}