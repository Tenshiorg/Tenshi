package io.github.shadow578.tenshi.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.util.ArrayList;

import io.github.shadow578.tenshi.BuildConfig;
import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.ui.MainActivity;
import io.github.shadow578.tenshi.util.EnumHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.collect;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.elvis;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;
import static io.github.shadow578.tenshi.util.TenshiPrefs.Key;
import static io.github.shadow578.tenshi.util.TenshiPrefs.Theme;
import static io.github.shadow578.tenshi.util.TenshiPrefs.getKeyName;

/**
 * main settings activity
 */
public class MainSettingsFragment extends PreferenceFragmentCompat {
    /**
     * click counter for the hidden developer options menu
     */
    private int hiddenMenuClicks = 0;

    /**
     * how many clicks are needed to unlock the developer options menu
     */
    private final int HIDDEN_MENU_CLICKS = 10;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // setup layout form preferences xml
        setPreferencesFromResource(R.xml.settings_main_screen, rootKey);

        // setup ui stuff
        final Context ctx = requireContext();
        setupThemeSelection(ctx);
        setupStartupSelection();
        setupLogoutButton(ctx);
        setupResetPrefsButton(ctx);
        setupAppVersion(ctx);
        setupDevOptions();
        setupAboutLibraries(ctx);
    }

    /**
     * setup the theme selection
     *
     * @param ctx the context to work in
     */
    private void setupThemeSelection(@NonNull Context ctx) {
        final ListPreference themePref = findPreference(getKeyName(Key.Theme));
        with(themePref, themes -> {
            final String[] themeValues = elvis(collect(Theme.values(), EnumHelper::valueOf), new ArrayList<String>()).toArray(new String[0]);
            themes.setEntries(themeValues);
            themes.setEntryValues(themeValues);
            themes.setDefaultValue(EnumHelper.valueOf(Theme.FollowSystem));

            // notify user to restart for theme to change
            themes.setOnPreferenceChangeListener((preference, newValue) -> {
                Toast.makeText(ctx, R.string.settings_toast_restart_for_theme_change, Toast.LENGTH_SHORT).show();
                return true;
            });
        });
    }

    /**
     * setup the startup tab selection
     */
    private void setupStartupSelection() {
        final ListPreference sectionPref = findPreference(getKeyName(Key.StartupSection));
        with(sectionPref, sections -> {
            final String[] tabValues = elvis(collect(MainActivity.Section.values(), EnumHelper::valueOf), new ArrayList<String>()).toArray(new String[0]);
            sections.setEntries(tabValues);
            sections.setEntryValues(tabValues);
            sections.setDefaultValue(EnumHelper.valueOf(MainActivity.Section.Home));
        });
    }

    /**
     * setup the logout button
     *
     * @param ctx the context to work in
     */
    private void setupLogoutButton(@NonNull Context ctx) {
        final Preference logoutPref = findPreference("logout_btn");
        with(logoutPref,
                logout -> logout.setOnPreferenceClickListener(preference -> {
                    new MaterialAlertDialogBuilder(ctx)
                            .setTitle(R.string.settings_dialog_confirm_logout)
                            .setPositiveButton(R.string.shared_dialog_confirmation_yes, (dialog, which) -> {
                                // invalidate token and redirect to login page
                                TenshiApp.INSTANCE.invalidateTokenAndLogin(requireActivity());
                            })
                            .setNegativeButton(R.string.shared_dialog_confirmation_no, null)
                            .show();
                    return true;
                }));
    }

    /**
     * setup the reset preferences button
     *
     * @param ctx the context to work in
     */
    private void setupResetPrefsButton(@NonNull Context ctx) {
        final Preference resetPref = findPreference("reset_prefs_btn");
        with(resetPref,
                reset -> reset.setOnPreferenceClickListener(preference -> {
                    new MaterialAlertDialogBuilder(ctx)
                            .setTitle(R.string.settings_dialog_confirm_reset_prefs)
                            .setPositiveButton(R.string.shared_dialog_confirmation_yes, (dialog, which) -> {
                                // reset all prefs and recreate
                                TenshiPrefs.clear();
                                requireActivity().recreate();
                            })
                            .setNegativeButton(R.string.shared_dialog_confirmation_no, null)
                            .show();
                    return true;
                }));
    }

    /**
     * setup the 'app version' preference to show the app version
     *
     * @param ctx the context to work in
     */
    private void setupAppVersion(@NonNull Context ctx) {
        // find app version category
        final Preference appVersionPref = findPreference("app_version");
        with(appVersionPref, appVersion -> {
            appVersion.setSummary(fmt("%s (%s)", BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE));
            appVersion.setOnPreferenceClickListener(preference -> {
                hiddenMenuClicks++;
                if (hiddenMenuClicks >= HIDDEN_MENU_CLICKS) {
                    final boolean wasAlreadyDev = TenshiPrefs.getBool(Key.ShowDeveloperOptions, false);

                    // set dev key
                    TenshiPrefs.setBool(Key.ShowDeveloperOptions, true);
                    setupDevOptions();

                    // show toast
                    if (wasAlreadyDev)
                        Toast.makeText(ctx, "You are already a developer", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ctx, "🎉 You are now a developer 🎉", Toast.LENGTH_SHORT).show();
                } else if (hiddenMenuClicks >= (HIDDEN_MENU_CLICKS * 0.6))
                    Toast.makeText(ctx, "🐱", Toast.LENGTH_SHORT).show();

                return true;
            });
        });

        // find app build date category
        final Preference buildDatePref = findPreference("app_build_date");
        with(buildDatePref, buildDate -> buildDate.setSummary(BuildConfig.BUILD_TIME_UTC + " (UTC)"));
    }

    /**
     * setup the developer options button
     */
    private void setupDevOptions() {
        final Preference devPref = findPreference("dev_options_btn");
        with(devPref, devOptions -> {
            devOptions.setVisible(TenshiPrefs.getBool(Key.ShowDeveloperOptions, false));
            devOptions.setFragment(DeveloperSettingsFragment.class.getName());
        });
    }

    /**
     * setup the about libraries preference
     *
     * @param ctx the context to work in
     */
    private void setupAboutLibraries(@NonNull Context ctx) {
        final Preference libPref = findPreference("about_libraries");
        with(libPref,
                about -> about.setOnPreferenceClickListener(preference -> {
                    final LibsBuilder libs = new LibsBuilder();
                    libs.setAboutAppName(getString(R.string.shared_app_name));
                    libs.start(ctx);
                    return true;
                }));
    }
}
