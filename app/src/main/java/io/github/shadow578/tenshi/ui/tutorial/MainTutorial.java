package io.github.shadow578.tenshi.ui.tutorial;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.ActivityMainBinding;
import io.github.shadow578.tenshi.ui.MainActivity;
import io.github.shadow578.tenshi.ui.fragments.HomeFragment;
import io.github.shadow578.tenshi.ui.fragments.ProfileFragment;
import io.github.shadow578.tenshi.ui.fragments.UserLibraryFragment;

/**
 * handles the tutorial for the {@link MainActivity}
 */
public class MainTutorial extends TutorialBase<MainActivity, ActivityMainBinding> {

    /**
     * current state
     */
    @NonNull
    private State state = State.ShowLibraryTab;

    private enum State {
        /**
         * show the user the home tab
         */
        ShowHomeTab,

        /**
         * show the user the library tab
         */
        ShowLibraryTab,

        /**
         * show the profile tab
         */
        ShowProfileTab,

        /**
         * show the MAL search bar
         */
        ShowSearch
    }

    @NonNull
    private final HomeFragment homeFrag;
    @NonNull
    private final UserLibraryFragment libFrag;
    @NonNull
    private final ProfileFragment proFrag;

    public MainTutorial(@NonNull MainActivity activity, @NonNull ActivityMainBinding binding,
                        @NonNull HomeFragment homeFragment, @NonNull UserLibraryFragment userLibraryFragment, @NonNull ProfileFragment profileFragment) {
        super(activity, binding);
        homeFrag = homeFragment;
        libFrag = userLibraryFragment;
        proFrag = profileFragment;
    }

    /**
     * start the tutorial sequence
     */
    public void start() {
        // reset state
        state = State.ShowLibraryTab;
        selectTab(MainActivity.Section.Home);

        // start sequence
        /* Start on Home Tab -> Show Library Tab
         * -> Show Profile Tab
         * -> Show Home Tab
         * -> Show Search Bar
         */
        new TapTargetSequence(a)
                .targets(
                        forView(b.bottomNav.findViewById(R.id.nav_library), R.string.tut_main_lib_title, R.string.tut_main_lib_text),
                        forView(b.bottomNav.findViewById(R.id.nav_profile), R.string.tut_main_profile_title, R.string.tut_main_profile_text),
                        forView(b.bottomNav.findViewById(R.id.nav_home), R.string.tut_main_home_title, R.string.tut_main_home_text),
                        forView(b.mainToolbar.findViewById(R.id.main_toolbar_text), R.string.tut_main_search_title, R.string.tut_main_search_text)
                )
                .listener(this)
                .start();
    }

    @Override
    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
        // update state
        nextState();
        switch (state) {
            case ShowProfileTab:
                selectTab(MainActivity.Section.Library);
                break;
            case ShowHomeTab:
                selectTab(MainActivity.Section.Profile);
                break;
            default:
            case ShowLibraryTab:
            case ShowSearch:
                selectTab(MainActivity.Section.Home);
                break;
        }
    }

    /**
     * increment to the next state.
     * if on the last state, calls finishTutorial()
     */
    private void nextState() {
        switch (state) {
            case ShowLibraryTab:
                state = State.ShowProfileTab;
                break;
            case ShowProfileTab:
                state = State.ShowHomeTab;
                break;
            case ShowHomeTab:
                state = State.ShowSearch;
                break;
            default:
            case ShowSearch:
                onTutorialEnd(false);
                break;
        }

    }

    /**
     * select and switch to a tab on the activity
     *
     * @param tab the tab to swtich to
     */
    private void selectTab(@NonNull MainActivity.Section tab) {
        // choose the fragment to open
        final Fragment targetFragment;
        switch (tab) {
            default:
            case Home:
                targetFragment = homeFrag;
                b.bottomNav.setSelectedItemId(R.id.nav_home);
                break;
            case Library:
                targetFragment = libFrag;
                b.bottomNav.setSelectedItemId(R.id.nav_library);
                break;
            case Profile:
                targetFragment = proFrag;
                b.bottomNav.setSelectedItemId(R.id.nav_profile);
                break;
        }

        // switch to the fragment
        a.getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_fragment_container, targetFragment)
                .commit();
    }
}
