package io.github.shadow578.tenshi.ui.tutorial;

import androidx.annotation.NonNull;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.ActivityAnimeDetailsBinding;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;

/**
 * handles the tutorial for the {@link AnimeDetailsActivity} on the first anime not in the user library
 */
public class AnimeDetailsNoLibTutorial extends TutorialBase<AnimeDetailsActivity, ActivityAnimeDetailsBinding> {

    public AnimeDetailsNoLibTutorial(@NonNull AnimeDetailsActivity activity, @NonNull ActivityAnimeDetailsBinding binding) {
        super(activity, binding);
    }

    /**
     * start the tutorial sequence
     */
    @Override
    public void start() {
        // disable all targeted buttons
        // otherwise, the tap target actually taps them
        setTargetViewsEnabled(false);

        // create and start sequence
        newSequence()
                // delete
                .add(newTarget().setTarget(b.animeAddToListBtn)
                        .setPrimaryText(R.string.tut_details_lib_add_title)
                        .setSecondaryText(R.string.tut_details_lib_add_text)
                )
                .start();
    }

    @Override
    protected void onEnd(boolean dismissed) {
        super.onEnd(dismissed);
        //re- enable targets
        setTargetViewsEnabled(true);
    }

    /**
     * set all views this tutorial targets enabled or disabled.
     * this is required as the tap target view forwards the click event to the views, thus activating them
     *
     * @param en enable the views?
     */
    private void setTargetViewsEnabled(boolean en) {
        b.animeAddToListBtn.setEnabled(en);
    }
}
