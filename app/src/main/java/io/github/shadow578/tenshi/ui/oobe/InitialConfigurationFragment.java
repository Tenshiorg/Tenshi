package io.github.shadow578.tenshi.ui.oobe;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashMap;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.FragmentInitialConfigurationBinding;
import io.github.shadow578.tenshi.databinding.RecyclerAnimeBigBinding;
import io.github.shadow578.tenshi.util.DateHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * fragment that handles initial app configuration.
 */
public class InitialConfigurationFragment extends OnboardingFragment {

    private FragmentInitialConfigurationBinding b;
    private final HashMap<TenshiPrefs.Theme, View> themePreviews = new HashMap<>();
    private boolean userConfirmedAge = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentInitialConfigurationBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        setupThemeSelector();
        b.nsfwToggle.setOnCheckedChangeListener((buttonView, isChecked) -> updateNSFWAfterCheck(isChecked));

        // tutorial skip btn
        b.skipTutorial.setOnCheckedChangeListener((buttonView, skipTut) -> {
            TenshiPrefs.setBool(TenshiPrefs.Key.MainTutorialFinished, skipTut);
            TenshiPrefs.setBool(TenshiPrefs.Key.AnimeDetailsNoLibTutorialFinished, skipTut);
            TenshiPrefs.setBool(TenshiPrefs.Key.AnimeDetailsInLibTutorialFinished, skipTut);
        });
    }

    //region theme

    /**
     * sets up the theme selection views and logic
     */
    private void setupThemeSelector() {
        // set state to current pref state
        final TenshiPrefs.Theme defaultTheme = TenshiPrefs.getEnum(TenshiPrefs.Key.Theme, TenshiPrefs.Theme.class, TenshiPrefs.Theme.FollowSystem);
        updateThemePreview(defaultTheme);
        switch (defaultTheme) {
            default:
            case FollowSystem:
                b.themeSelectRadioGroup.check(R.id.theme_select_follow_system);
                break;
            case Light:
                b.themeSelectRadioGroup.check(R.id.theme_select_light);
                break;
            case Dark:
                b.themeSelectRadioGroup.check(R.id.theme_select_dark);
                break;
            case Amoled:
                b.themeSelectRadioGroup.check(R.id.theme_select_amoled);
                break;
        }

        // change listener, update preview and pref
        b.themeSelectRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            final TenshiPrefs.Theme theme;
            if (checkedId == R.id.theme_select_light)
                theme = TenshiPrefs.Theme.Light;
            else if (checkedId == R.id.theme_select_dark)
                theme = TenshiPrefs.Theme.Dark;
            else if (checkedId == R.id.theme_select_amoled)
                theme = TenshiPrefs.Theme.Amoled;
            else //default to follow_system
                theme = TenshiPrefs.Theme.FollowSystem;

            updateThemePreview(theme);
            TenshiPrefs.setEnum(TenshiPrefs.Key.Theme, theme);
        });
    }

    /**
     * update the theme preview
     *
     * @param theme the theme to show
     */
    private void updateThemePreview(@NonNull TenshiPrefs.Theme theme) {
        // inflate preview if needed
        if (!themePreviews.containsKey(theme))
            themePreviews.put(theme, createThemePreview(theme));

        // make the right preview visible
        for (TenshiPrefs.Theme t : themePreviews.keySet()) {
            final View v = themePreviews.get(t);
            if (notNull(v))
                v.setVisibility(t.equals(theme) ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * create a view for the theme preview, with the theme overwritten
     *
     * @param theme the theme to use
     * @return the inflated view
     */
    private View createThemePreview(@NonNull TenshiPrefs.Theme theme) {
        // create context for preview, with the theme overwritten
        final Context baseCtx = requireContext();
        final Context themeCtx = wrapContextForTheme(baseCtx, theme);

        // inflate the preview layout with the overwritten view
        final RecyclerAnimeBigBinding tpb = RecyclerAnimeBigBinding.inflate(LayoutInflater.from(themeCtx));
        tpb.animeMainPoster.setImageDrawable(ContextCompat.getDrawable(baseCtx, R.drawable.ic_icon_24));

        // update the layout params to include margins and fill the container
        final int marginPxs = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, themeCtx.getResources().getDisplayMetrics());
        final CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(marginPxs, marginPxs, marginPxs, marginPxs);
        tpb.getRoot().setLayoutParams(params);

        // initially hide the view
        tpb.getRoot().setVisibility(View.GONE);
        b.themePreviewContainer.addView(tpb.getRoot());
        return tpb.getRoot();
    }

    /**
     * create a wrapped context that overrides the app theme
     *
     * @param baseCtx the base context
     * @param theme   the theme to use
     * @return the wrapped context
     */
    private Context wrapContextForTheme(@NonNull Context baseCtx, @NonNull TenshiPrefs.Theme theme) {
        final @StyleRes int themeRes;
        final int newNightMode;
        switch (theme) {
            case Amoled:
                newNightMode = Configuration.UI_MODE_NIGHT_YES;
                themeRes = R.style.TenshiTheme_Amoled;
                break;
            case Light:
                newNightMode = Configuration.UI_MODE_NIGHT_NO;
                themeRes = R.style.TenshiTheme;
                break;
            case Dark:
                newNightMode = Configuration.UI_MODE_NIGHT_YES;
                themeRes = R.style.TenshiTheme;
                break;
            default:
            case FollowSystem:
                // if we follow the system, get the default value from the application context
                final Configuration appCfg = baseCtx.getApplicationContext().getResources().getConfiguration();
                newNightMode = appCfg.uiMode & Configuration.UI_MODE_NIGHT_MASK;

                // we use the normal theme
                themeRes = R.style.TenshiTheme;
                break;
        }

        // create wrapped context
        final ContextThemeWrapper wrappedCtx = new ContextThemeWrapper(baseCtx, themeRes);

        // set the night mode in the config, and update it after
        final Configuration cfg = wrappedCtx.getResources().getConfiguration();
        cfg.uiMode = newNightMode | (cfg.uiMode & ~Configuration.UI_MODE_NIGHT_MASK);
        wrappedCtx.getResources().updateConfiguration(cfg, wrappedCtx.getResources().getDisplayMetrics());

        return wrappedCtx;
    }
    //endregion

    //region NSFW toggle

    /**
     * update the NSFW preference after checking if the user is legal age.
     * if the users age is not clear, a dialog is shown to confirm the age.
     *
     * @param showNSFW new NSFW setting
     */
    private void updateNSFWAfterCheck(boolean showNSFW) {
        // disable nsfw? no problem here
        if (!showNSFW) {
            TenshiPrefs.setBool(TenshiPrefs.Key.NSFW, false);
            return;
        }

        // enable nsfw? check if legal age first
        if (isUserLegalAge() || userConfirmedAge) {
            // user has birthday in MAL and is 18+, or has already confirmed their age. It's all ok officer
            TenshiPrefs.setBool(TenshiPrefs.Key.NSFW, true);
        } else {
            // unbreak dialog theme
            wrapContextForTheme(requireContext(), TenshiPrefs.Theme.FollowSystem);

            // user has no birthday in MAL or is not 18+ according to that date, ask directly
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.oobe_config_nsfw_confirm_title)
                    .setMessage(R.string.oobe_config_nsfw_confirm_message)
                    .setPositiveButton(R.string.oobe_config_nsfw_confirm_yes, (dialog, which) -> {
                        // i guess it's fine if the user says so... they wouldn't lie, would they?
                        TenshiPrefs.setBool(TenshiPrefs.Key.NSFW, true);
                        userConfirmedAge = true;
                    })
                    .setNegativeButton(R.string.oobe_config_nsfw_confirm_no, (dialog, which) -> {
                        // at least they're honest
                        TenshiPrefs.setBool(TenshiPrefs.Key.NSFW, false);
                        userConfirmedAge = false;
                        b.nsfwToggle.setChecked(false);
                    })
                    .show();
        }
    }

    /**
     * check if the shared.user is of legal age (18+)
     *
     * @return is the user legal?
     */
    private boolean isUserLegalAge() {
        if (notNull(shared.user) && notNull(shared.user.birthday)) {
            // check if 18+
            return DateHelper.getYearsToNow(shared.user.birthday) >= 18;
        }

        // no data, assume not legal age
        return false;
    }
    //endregion
}
