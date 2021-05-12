package io.github.shadow578.tenshi.ui.tutorial;

import org.jetbrains.annotations.NotNull;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.ActivityMainBinding;
import io.github.shadow578.tenshi.ui.MainActivity;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

/**
 * handles the tutorial for the {@link MainActivity}
 */
public class MainTutorial extends TutorialBase<MainActivity, ActivityMainBinding> {

    public MainTutorial(@NotNull MainActivity activity, @NotNull ActivityMainBinding binding) {
        super(activity, binding);
    }

    /**
     * start the tutorial sequence
     */
    public void start() {
        // disable toolbar (search click)
        b.mainToolbar.setEnabled(false);

        // create the sequence and start it
        newSequence()
                // library
                .add(newTarget().setTarget(b.bottomNav.findViewById(R.id.nav_library))
                        .setPrimaryText(R.string.tut_main_lib_title)
                        .setSecondaryText(R.string.tut_main_lib_text)
                )
                // profile
                .add(newTarget().setTarget(b.bottomNav.findViewById(R.id.nav_profile))
                        .setPrimaryText(R.string.tut_main_profile_title)
                        .setSecondaryText(R.string.tut_main_profile_text)
                )
                // home
                .add(newTarget().setTarget(b.bottomNav.findViewById(R.id.nav_home))
                        .setPrimaryText(R.string.tut_main_home_title)
                        .setSecondaryText(R.string.tut_main_home_text)
                )
                // search
                .add(newTarget().setTarget(b.mainToolbar.findViewById(R.id.main_toolbar))
                        .setPrimaryText(R.string.tut_main_search_title)
                        .setSecondaryText(R.string.tut_main_search_text)
                        .setPromptFocal(new RectanglePromptFocal())
                        .setPromptBackground(new RectanglePromptBackground())
                )
                // and start it
                .start();
    }

    @Override
    protected void onEnd(boolean dismissed) {
        super.onEnd(dismissed);
        b.mainToolbar.setEnabled(true);
    }
}
