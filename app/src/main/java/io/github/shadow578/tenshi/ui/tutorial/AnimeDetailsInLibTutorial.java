package io.github.shadow578.tenshi.ui.tutorial;

import androidx.annotation.NonNull;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.ActivityAnimeDetailsBinding;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.listOf;

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
        // add basic targets
        ArrayList<TapTarget> targets = listOf(
                forView(b.animeEditListStatusBtn, R.string.tut_details_status_title, R.string.tut_details_status_text),
                forView(b.animeEditEpisodeCountBtn, R.string.tut_details_eps_title, R.string.tut_details_eps_text),
                forView(b.animeEditRatingBtn, R.string.tut_details_rating_title, R.string.tut_details_rating_text),
                configure(TapTarget.forToolbarMenuItem(b.animeDetailsToolbar, R.id.share, a.getString(R.string.tut_details_share_title), a.getString(R.string.tut_details_share_text)))
        );

        // add watch now target
        if (hasWatchNow) {
            targets.add(forView(b.animeWatchNowButton, R.string.tut_details_watch_title, R.string.tut_details_watch_text));
        }

        // start sequence
        new TapTargetSequence(a)
                .targets(targets)
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
