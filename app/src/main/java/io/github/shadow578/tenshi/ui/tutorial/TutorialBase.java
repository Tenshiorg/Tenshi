package io.github.shadow578.tenshi.ui.tutorial;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.viewbinding.ViewBinding;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.color.MaterialColors;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.extensionslib.lang.Consumer;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * base class for activity tutorials.
 * invokes the {@link #setEndListener(Consumer)} when the tutorial finishes or is cancelled
 *
 * @param <ActivityClass>   the activity this is a tutorial for
 * @param <ActivityBinding> the activities binding type
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public abstract class TutorialBase<ActivityClass extends Activity, ActivityBinding extends ViewBinding> implements TapTargetSequence.Listener {

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
     * {@link TapTargetSequence.Listener#onSequenceStep(TapTarget, boolean)}
     */
    @Override
    public abstract void onSequenceStep(TapTarget lastTarget, boolean targetClicked);

    @Override
    public void onSequenceFinish() {
        onTutorialEnd(false);
    }

    @Override
    public void onSequenceCanceled(TapTarget lastTarget) {
        onTutorialEnd(true);
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
     * create a tap target for a view
     *
     * @param v     the view to create the target for
     * @param title the title of the target
     * @param text  the message text of the target
     * @return the tap target
     */
    @NonNull
    protected TapTarget forView(@NonNull View v, @StringRes int title, @StringRes int text) {
        return forView(v, a.getString(title), a.getString(text));
    }

    /**
     * create a tap target for a view
     *
     * @param v     the view to create the target for
     * @param title the title of the target
     * @param text  the message text of the target
     * @return the tap target
     */
    @NonNull
    protected TapTarget forView(@NonNull View v, @NonNull String title, @NonNull String text) {
        return configure(TapTarget.forView(v, title, text));
    }

    /**
     * configure a tap target to the default config
     * @param t the target
     * @return the target
     */
    @NonNull
    protected TapTarget configure(@NonNull TapTarget t){
        // do not tint the target by default
        // also, would be nice to use the current app theme
        return t.tintTarget(false)
                .outerCircleColorInt(MaterialColors.getColor(b.getRoot(), R.attr.colorPrimary))
                .titleTextColorInt(MaterialColors.getColor(b.getRoot(), R.attr.colorOnPrimary))
                .textColorInt(MaterialColors.getColor(b.getRoot(), R.attr.colorOnPrimary));

    }
}
