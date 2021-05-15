package io.github.shadow578.tenshi.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mikepenz.aboutlibraries.LibsBuilder;

import io.github.shadow578.tenshi.BuildConfig;
import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.ui.MainActivity;
import io.github.shadow578.tenshi.util.DateHelper;
import io.github.shadow578.tenshi.util.EnumHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.async;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.listOf;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;
import static io.github.shadow578.tenshi.util.TenshiPrefs.Key;
import static io.github.shadow578.tenshi.util.TenshiPrefs.Theme;
import static io.github.shadow578.tenshi.util.TenshiPrefs.getKeyName;

/**
 * main settings activity
 */
public class MainSettingsFragment extends PreferenceFragmentCompat {

    /**
     * how many clicks are needed to unlock the developer options menu
     */
    private final int HIDDEN_MENU_CLICKS = 10;

    /**
     * click counter for the hidden developer options menu
     */
    private int hiddenMenuClicks = 0;

    /**
     * is the user legal age? (18+)
     * set by either checking the birthdate of the user in MAL, or by showing a dialog
     */
    private boolean userIsLegalAge = false;

    /**
     * context reference. this is set before any setup*() function is called
     */
    private Context ctx;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // get context
        ctx = requireContext();

        // setup layout form preferences xml
        setPreferencesFromResource(R.xml.settings_main_screen, rootKey);

        // setup ui stuff
        setupThemeSelection();
        setupStartupSelection();
        setupNSFWToggle();
        setupResetTutorialButton();
        setupLogoutButton();
        setupResetPrefsButton();
        setupAppVersion();
        setupDevOptions();
        setupAboutLibraries();
    }

    /**
     * setup the theme selection
     */
    private void setupThemeSelection() {
        final ListPreference themePref = findPreference(getKeyName(Key.Theme));
        with(themePref, themes -> {
            // prepare property values and their display name
            // the lists have to be in the same order
            final String[] themeValues = listOf(
                    EnumHelper.valueOf(Theme.FollowSystem),
                    EnumHelper.valueOf(Theme.Light),
                    EnumHelper.valueOf(Theme.Dark),
                    EnumHelper.valueOf(Theme.Amoled)
            ).toArray(new String[0]);
            final String[] themeNames = listOf(
                    ctx.getString(R.string.settings_theme_follow_system),
                    ctx.getString(R.string.settings_theme_light),
                    ctx.getString(R.string.settings_theme_dark),
                    ctx.getString(R.string.settings_theme_amoled)
            ).toArray(new String[0]);

            // setup preference
            themes.setEntries(themeNames);
            themes.setEntryValues(themeValues);
            themes.setDefaultValue(themeValues[0]);

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
            // prepare property values and their display name
            // the lists have to be in the same order
            final String[] tabValues = listOf(
                    EnumHelper.valueOf(MainActivity.Section.Home),
                    EnumHelper.valueOf(MainActivity.Section.Library),
                    EnumHelper.valueOf(MainActivity.Section.Profile)
            ).toArray(new String[0]);
            final String[] tabNames = listOf(
                    ctx.getString(R.string.settings_start_tab_home),
                    ctx.getString(R.string.settings_start_tab_library),
                    ctx.getString(R.string.settings_start_tab_profile)
            ).toArray(new String[0]);

            // setup preference
            sections.setEntries(tabNames);
            sections.setEntryValues(tabValues);
            sections.setDefaultValue(tabValues[0]);
        });
    }

    /**
     * setup the age confirmation on the NSFW toggle
     */
    private void setupNSFWToggle() {
        // setup the switch
        final SwitchPreferenceCompat nsfwPref = findPreference(getKeyName(Key.NSFW));
        with(nsfwPref, nsfw -> nsfw.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof Boolean && (boolean) newValue) {
                if (!userIsLegalAge) {
                    // seems like the user is not legal age (or we don't know)
                    // ask nicely :P
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.settings_dialog_confirm_nsfw_title)
                            .setMessage(R.string.settings_dialog_confirm_nsfw_text)
                            .setPositiveButton(R.string.settings_dialog_confirm_nsfw_yes, (dialog, which) -> {
                                // i guess it's fine if the user says so... they wouldn't lie, would they?
                                TenshiPrefs.setBool(TenshiPrefs.Key.NSFW, true);
                                userIsLegalAge = true;
                            })
                            .setNegativeButton(R.string.settings_dialog_confirm_nsfw_no, (dialog, which) -> {
                                // at least they're honest
                                TenshiPrefs.setBool(TenshiPrefs.Key.NSFW, false);
                                userIsLegalAge = false;
                                nsfw.setChecked(false);
                            })
                            .show();
                }
            }
            return true;
        }));

        // get the user from DB (should be preloaded from OOBE)
        async(() -> {
            int userId = TenshiPrefs.getInt(TenshiPrefs.Key.UserID, -1);
            if (userId != -1)
                return TenshiApp.getDB().userDB().getUserById(userId);
            else
                return null;
        }, u -> {
            if (notNull(u) && notNull(u.birthday)) {
                // check if 18+
                userIsLegalAge = DateHelper.getYearsToNow(u.birthday) >= 18;
            } else {
                // no details on user, assume not legal
                userIsLegalAge = false;
            }
        });
    }

    /**
     * setup the tutorial reset button
     */
    private void setupResetTutorialButton() {
        final Preference tutPref = findPreference("reset_tutorials_btn");
        with(tutPref,
                tut -> tut.setOnPreferenceClickListener(preference -> {
                    TenshiPrefs.clear(Key.MainTutorialFinished);
                    TenshiPrefs.clear(Key.AnimeDetailsNoLibTutorialFinished);
                    TenshiPrefs.clear(Key.AnimeDetailsInLibTutorialFinished);
                    Toast.makeText(ctx, R.string.settings_toast_tut_reset, Toast.LENGTH_SHORT).show();
                    return true;
                }));
    }

    /**
     * setup the logout button
     */
    private void setupLogoutButton() {
        final Preference logoutPref = findPreference("logout_btn");
        with(logoutPref,
                logout -> logout.setOnPreferenceClickListener(preference -> {
                    new MaterialAlertDialogBuilder(ctx)
                            .setTitle(R.string.settings_dialog_confirm_logout_title)
                            .setMessage(R.string.settings_dialog_confirm_logout_text)
                            .setPositiveButton(R.string.shared_dialog_confirmation_yes, (dialog, which) -> {
                                // invalidate token and redirect to login page
                                TenshiApp.INSTANCE.logoutAndLogin(requireActivity());
                            })
                            .setNegativeButton(R.string.shared_dialog_confirmation_no, null)
                            .show();
                    return true;
                }));
    }

    /**
     * setup the reset preferences button
     */
    private void setupResetPrefsButton() {
        final Preference resetPref = findPreference("reset_prefs_btn");
        with(resetPref,
                reset -> reset.setOnPreferenceClickListener(preference -> {
                    new MaterialAlertDialogBuilder(ctx)
                            .setTitle(R.string.settings_dialog_confirm_reset_prefs_title)
                            .setMessage(R.string.settings_dialog_confirm_reset_prefs_text)
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
     */
    private void setupAppVersion() {
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
     */
    private void setupAboutLibraries() {
        final Preference libPref = findPreference("about_libraries");
        with(libPref,
                about -> about.setOnPreferenceClickListener(preference -> {
                    final LibsBuilder libs = new LibsBuilder();
                    libs.setAboutAppName(getString(R.string.shared_app_label));
                    libs.start(ctx);
                    return true;
                }));
    }
}
