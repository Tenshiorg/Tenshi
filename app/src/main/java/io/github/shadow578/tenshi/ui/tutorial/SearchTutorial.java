package io.github.shadow578.tenshi.ui.tutorial;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.ActivitySearchBinding;
import io.github.shadow578.tenshi.ui.search.SearchActivity;

/**
 * (short) tutorial for the {@link SearchActivity}
 */
public class SearchTutorial extends TutorialBase<SearchActivity, ActivitySearchBinding> {
    public SearchTutorial(@NonNull @NotNull SearchActivity activity, @NonNull @NotNull ActivitySearchBinding binding) {
        super(activity, binding);
    }

    /**
     * start the tutorial sequence
     */
    @Override
    public void start() {
        newSequence()
                // only have to show image search, the rest should be easy to understand :D
                .add(newTarget().setTarget(b.openImageSearch)
                        .setPrimaryText(R.string.tut_search_trace_title)
                        .setSecondaryText(R.string.tut_search_trace_text))
                .start();
    }
}
