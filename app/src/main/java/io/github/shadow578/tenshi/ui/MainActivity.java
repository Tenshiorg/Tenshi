package io.github.shadow578.tenshi.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.platform.MaterialFadeThrough;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.ActivityMainBinding;
import io.github.shadow578.tenshi.ui.fragments.HomeFragment;
import io.github.shadow578.tenshi.ui.fragments.ProfileFragment;
import io.github.shadow578.tenshi.ui.fragments.UserLibraryFragment;
import io.github.shadow578.tenshi.ui.settings.SettingsActivity;
import io.github.shadow578.tenshi.util.EnumHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.lang.LanguageUtils.nullOrEmpty;
import static io.github.shadow578.tenshi.lang.LanguageUtils.with;

/**
 * the main activity
 */
public class MainActivity extends TenshiActivity {
    /**
     * Request code to use if a anime in the user library was updated.
     * This will force a refresh of the data
     */
    public static final int REQUEST_UPDATE_ANIME = 77;

    /**
     * Request code to use after login.
     */
    public static final int REQUEST_LOGIN = 2;

    /**
     * the sections of the main activity
     */
    public enum Section {
        Home,
        Library,
        Profile
    }

    private final HomeFragment homeFragment = new HomeFragment();
    private final UserLibraryFragment libraryFragment = new UserLibraryFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private ActivityMainBinding b;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // check user is logged in, redirect to login and finish() if not
        requireUserAuthenticated();

        //region set theme
        switch (TenshiPrefs.getEnum(TenshiPrefs.Key.Theme, TenshiPrefs.Theme.class, TenshiPrefs.Theme.FollowSystem)) {
            case Light:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Dark:
            case Amoled:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case FollowSystem:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        //endregion

        // setup toolbar
        setSupportActionBar(b.mainToolbar);
        with(getSupportActionBar(), sab -> {
            sab.setDisplayHomeAsUpEnabled(true);
            sab.setHomeButtonEnabled(true);
            sab.setDisplayShowTitleEnabled(false);
        });
        b.mainToolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24);

        // open settings from main toolbar
        b.mainToolbar.setNavigationOnClickListener(v -> openSettings());

        // open search from main toolbar
        b.mainToolbar.setOnClickListener(v -> openSearch());

        // is logged in, proceed setting up activity
        setFragmentTransitions();
        setupNavBar(b.bottomNav, TenshiPrefs.getEnum(TenshiPrefs.Key.StartupSection, Section.class, Section.Home));
    }

    /**
     * setup the bottom navigation bar.
     * check intent for actions from shortcuts
     *
     * @param navigationView the bottom navigation view to setup
     * @param defaultSection the default section to open, loaded from prefsf
     */
    private void setupNavBar(@NonNull BottomNavigationView navigationView, @NonNull Section defaultSection) {
        // did the app open from a home screen shortcut?
        final String action = getIntent().getAction();
        Section targetSection = defaultSection;
        if (!nullOrEmpty(action) && !action.equals(Intent.ACTION_MAIN))
            targetSection = EnumHelper.parseEnum(Section.class, action, defaultSection, true);

        // choose the fragment to open
        Fragment targetFragment;
        switch (targetSection) {
            default:
            case Home:
                targetFragment = homeFragment;
                navigationView.setSelectedItemId(R.id.nav_home);
                break;
            case Library:
                targetFragment = libraryFragment;
                navigationView.setSelectedItemId(R.id.nav_library);
                break;
            case Profile:
                targetFragment = profileFragment;
                navigationView.setSelectedItemId(R.id.nav_profile);
                break;
        }

        // switch to the fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_fragment_container, targetFragment)
                .commit();

        // add navigation listener to navbar
        navigationView.setOnNavigationItemSelectedListener(item -> {
            // get the fragment to open
            Fragment target;
            if (item.getItemId() == R.id.nav_library)
                target = libraryFragment;
            else if (item.getItemId() == R.id.nav_profile)
                target = profileFragment;
            else
                target = homeFragment;

            // switch to the target
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_fragment_container, target)
                    .commit();

            return true;
        });
    }

    /**
     * initialize transitions for home, library and profile fragments
     */
    private void setFragmentTransitions() {
        // prepare common transition for all fragments
        final MaterialFadeThrough f = new MaterialFadeThrough();

        // set on home fragment
        homeFragment.setEnterTransition(f);
        homeFragment.setExitTransition(f);

        // ... anime list fragment
        libraryFragment.setEnterTransition(f);
        libraryFragment.setExitTransition(f);

        // ... profile fragment
        profileFragment.setEnterTransition(f);
        profileFragment.setExitTransition(f);
    }

    /**
     * open the search activity
     */
    public void openSearch() {
        final Intent i = new Intent(this, SearchActivity.class);
        final ActivityOptionsCompat opt = ActivityOptionsCompat.makeSceneTransitionAnimation(this, b.mainToolbar, b.mainToolbar.getTransitionName());
        startActivity(i, opt.toBundle());
    }

    /**
     * open the settings activity
     */
    public void openSettings() {
        final Intent i = new Intent(this, SettingsActivity.class);
        final ActivityOptionsCompat opt = ActivityOptionsCompat.makeSceneTransitionAnimation(this, b.mainToolbar, b.mainToolbar.getTransitionName());
        startActivity(i, opt.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == REQUEST_UPDATE_ANIME)
                this.recreate();
    }
}
