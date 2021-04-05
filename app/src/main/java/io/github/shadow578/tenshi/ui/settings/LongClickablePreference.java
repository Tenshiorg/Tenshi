package io.github.shadow578.tenshi.ui.settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import io.github.shadow578.tenshi.extensionslib.lang.Function;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * a preference with a long click listener
 */
public class LongClickablePreference extends Preference {

    /**
     * listener for long clicks
     */
    @Nullable
    private Function<Boolean, LongClickablePreference> longClickListener;

    public LongClickablePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LongClickablePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LongClickablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LongClickablePreference(Context context) {
        super(context);
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnLongClickListener(v -> {
            if (notNull(longClickListener))
                return longClickListener.invoke(this);
            return false;
        });
    }

    /**
     * set a long click listener on this preference
     *
     * @param listener the listener to set
     */
    public void setOnPreferenceLongClickListener(@Nullable Function<Boolean, LongClickablePreference> listener) {
        longClickListener = listener;
    }
}
