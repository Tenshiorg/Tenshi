package io.github.shadow578.tenshi.ui.tutorial;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.color.MaterialColors;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.extensionslib.lang.BiConsumer;
import io.github.shadow578.tenshi.extensionslib.lang.Consumer;
import io.github.shadow578.tenshi.ui.tutorial.taptarget.TapTargetSequence;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * base class for activity tutorials.
 * invokes the {@link #setEndListener(Consumer)} when the tutorial finishes or is cancelled
 *
 * @param <ActivityClass>   the activity this is a tutorial for
 * @param <ActivityBinding> the activities binding type
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public abstract class TutorialBase<ActivityClass extends Activity, ActivityBinding extends ViewBinding> {

    /**
     * activity instance
     */
    @NonNull
    protected final ActivityClass a;

    /**
     * activity binding
     */
    @NonNull
    protected final ActivityBinding b;

    /**
     * tutorial end listener
     */
    @Nullable
    private Consumer<Boolean> endListener;

    public TutorialBase(@NonNull ActivityClass activity, @NonNull ActivityBinding binding) {
        a = activity;
        b = binding;
    }

    /**
     * start the tutorial sequence
     */
    public abstract void start();

    /**
     * listener set for {@link TapTargetSequence#onStep(BiConsumer)} when using {@link #newSequence()}
     *
     * @param current the currently shown item
     * @param next    the upcoming item
     */
    protected void onStep(@NonNull TapTargetSequence.Item current, @NonNull TapTargetSequence.Item next) {

    }

    /**
     * listener set for {@link TapTargetSequence#onEnd(Consumer)} when using {@link #newSequence()}
     *
     * @param dismissed was the sequence dismissed?
     */
    protected void onEnd(boolean dismissed) {
        onTutorialEnd(dismissed);
    }

    /**
     * set the tutorial end listener
     *
     * @param listener the listener to set
     */
    @NonNull
    public TutorialBase<ActivityClass, ActivityBinding> setEndListener(@Nullable Consumer<Boolean> listener) {
        endListener = listener;
        return this;
    }

    /**
     * invoke the tutorial end listener
     *
     * @param canceled was the tutorial cancelled?
     */
    protected void onTutorialEnd(boolean canceled) {
        if (notNull(endListener))
            endListener.invoke(canceled);
    }

    /**
     * create a new sequence with listeners set
     *
     * @return the sequence
     */
    protected TapTargetSequence newSequence() {
        return new TapTargetSequence()
                .onStep(this::onStep)
                .onEnd(this::onEnd);
    }

    /**
     * create a new (default) tap target builder
     *
     * @return the builder instance
     */
    protected MaterialTapTargetPrompt.Builder newTarget() {
        return new MaterialTapTargetPrompt.Builder(a)
                .setBackgroundColour(MaterialColors.getColor(b.getRoot(), R.attr.colorPrimary))
                //.setFocalColour()
                .setPrimaryTextColour(MaterialColors.getColor(b.getRoot(), R.attr.colorOnPrimary))
                .setSecondaryTextColour(MaterialColors.getColor(b.getRoot(), R.attr.colorOnPrimary));
    }
}
