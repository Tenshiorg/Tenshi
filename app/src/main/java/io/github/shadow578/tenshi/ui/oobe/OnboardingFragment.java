package io.github.shadow578.tenshi.ui.oobe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

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

    @NonNull
    protected static SharedOOBEProperties shared = new SharedOOBEProperties();

    @Nullable
    private TabLayout.Tab dotTab =null;

    /**
     * init and add a tab for this fragment in the given table layout
     *
     * @param tabLayout the tab layout to add to
     */
    public void initTab(@NonNull TabLayout tabLayout) {
        dotTab = tabLayout.newTab();
        dotTab.view.setEnabled(false);
        tabLayout.addTab(dotTab);
    }

    /**
     * get the tab initialized with initTab
     *
     * @return the tab
     */
    @Nullable
    public TabLayout.Tab getTab() {
        return dotTab;
    }

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
