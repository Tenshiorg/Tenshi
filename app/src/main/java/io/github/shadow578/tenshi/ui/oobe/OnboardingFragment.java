package io.github.shadow578.tenshi.ui.oobe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.shadow578.tenshi.extensionslib.lang.Method;
import io.github.shadow578.tenshi.ui.fragments.TenshiFragment;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * base class for all onboarding fragments.
 * - Provides OnSuccess and OnFail listeners
 */
@SuppressWarnings({"UnusedReturnValue", "unused", "RedundantSuppression"})
public abstract class OnboardingFragment extends TenshiFragment {

    @Nullable
    private Method successListener;

    @Nullable
    private Method failListener;

    /**
     * set the onSuccess listener
     *
     * @param listener the listener
     * @return instance
     */
    @NonNull
    public OnboardingFragment setOnSuccessListener(@NonNull Method listener) {
        successListener = listener;
        return this;
    }

    /**
     * set the onFail listener
     *
     * @param listener the listener
     * @return instance
     */
    @NonNull
    public OnboardingFragment setOnFailListener(@NonNull Method listener) {
        failListener = listener;
        return this;
    }

    /**
     * invoke the onSuccess listener
     */
    protected void invokeSuccessListener() {
        if (notNull(successListener))
            successListener.invoke();
    }

    /**
     * invoke the onFail listener
     */
    protected void invokeFailListener() {
        if (notNull(failListener))
            failListener.invoke();
    }
}
