package io.github.shadow578.tenshi.ui.tutorial;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.ActivityAnimeDetailsBinding;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;
import io.github.shadow578.tenshi.ui.tutorial.taptarget.TapTargetSequence;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

/**
 * handles the tutorial for the {@link AnimeDetailsActivity} on the first anime that is in the user library
 */
public class AnimeDetailsInLibTutorial extends TutorialBase<AnimeDetailsActivity, ActivityAnimeDetailsBinding> {

    private final boolean hasWatchNow;

    public AnimeDetailsInLibTutorial(@NonNull AnimeDetailsActivity activity, @NonNull ActivityAnimeDetailsBinding binding, boolean watchNowAvailable) {
        super(activity, binding);
        hasWatchNow = watchNowAvailable;
    }

    /**
     * start the tutorial sequence
     */
    @Override
    public void start() {
        // make sure we scrolled to the top (all views should be visible)
        b.animeMainContentGroup.smoothScrollTo(0, 0);

        // disable all targeted buttons
        // otherwise, the tap target actually taps them
        setTargetViewsEnabled(false);

        // add basic targets
        final TapTargetSequence sequence = newSequence()
                // share
                .add(newTarget().setTarget(b.animeDetailsToolbar.findViewById(R.id.share))
                                .setPrimaryText(R.string.tut_details_share_title)
                                .setSecondaryText(R.string.tut_details_share_text),
                        0
                )
                // status
                .add(newTarget().setTarget(b.animeEditListStatusBtn)
                                .setPrimaryText(R.string.tut_details_status_title)
                                .setSecondaryText(R.string.tut_details_status_text),
                        1
                )
                // ep count
                .add(newTarget().setTarget(b.animeEditEpisodeCountBtn)
                                .setPrimaryText(R.string.tut_details_eps_title)
                                .setSecondaryText(R.string.tut_details_eps_text),
                        2
                )
                // rating
                .add(newTarget().setTarget(b.animeEditRatingBtn)
                                .setPrimaryText(R.string.tut_details_rating_title)
                                .setSecondaryText(R.string.tut_details_rating_text),
                        3
                );

        // add watch now target
        if (hasWatchNow)
            sequence.add(newTarget().setTarget(b.animeWatchNowButton)
                            .setPrimaryText(R.string.tut_details_watch_title)
                            .setSecondaryText(R.string.tut_details_watch_text),
                    4
            );

        // add delete button
        sequence.add(newTarget().setTarget(b.detailsDeleteButton)
                        .setPrimaryText(R.string.tut_details_delete_title)
                        .setSecondaryText(R.string.tut_details_delete_text)
                        .setPromptFocal(new RectanglePromptFocal())
                        .setPromptBackground(new RectanglePromptBackground()),
                5
        );

        // start the sequence
        sequence.start();
    }

    @Override
    protected void onStep(@NonNull @NotNull TapTargetSequence.Item current, @NonNull @NotNull TapTargetSequence.Item next) {
        super.onStep(current, next);
        if (next.id == 5) {
            // upcoming is the delete button, scroll there
            b.animeMainContentGroup.requestChildFocus(b.detailsDeleteGroup, b.detailsDeleteButton);
        }
    }

    @Override
    protected void onEnd(boolean dismissed) {
        super.onEnd(dismissed);

        //re- enable targets
        setTargetViewsEnabled(true);

        // scroll back to the top (initial position)
        b.animeMainContentGroup.smoothScrollTo(0, 0);
    }

    /**
     * set all views this tutorial targets enabled or disabled.
     * this is required as the tap target view forwards the click event to the views, thus activating them
     *
     * @param en enable the views?
     */
    private void setTargetViewsEnabled(boolean en) {
        b.animeDetailsToolbar.findViewById(R.id.share).setEnabled(en);
        b.animeEditListStatusBtn.setEnabled(en);
        b.animeEditEpisodeCountBtn.setEnabled(en);
        b.animeEditRatingBtn.setEnabled(en);
        b.animeWatchNowButton.setEnabled(en);
        b.detailsDeleteButton.setEnabled(en);
    }
}
