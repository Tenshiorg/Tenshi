package io.github.shadow578.tenshi.ui.tutorial;

import androidx.annotation.NonNull;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

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
        new TapTargetSequence(a)
                .targets(
                        forView(b.animeAddToListBtn, R.string.tut_details_lib_add_title, R.string.tut_details_lib_add_text)
                )
                .listener(this)
                .start();
    }

    /**
     * {@link TapTargetSequence.Listener#onSequenceStep(TapTarget, boolean)}
     */
    @Override
    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

    }
}
