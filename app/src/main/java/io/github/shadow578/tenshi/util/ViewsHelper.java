package io.github.shadow578.tenshi.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;

/**
 * helper class for measuring and working with views
 */
public class ViewsHelper {
    /**
     * Measure the content width a ListAdapter requires for its content.
     * Workaround for ListPopoutWindow not accepting WRAP_CONTENT as width
     * https://stackoverflow.com/a/26814964
     *
     * @param ctx the context to work in
     * @param adapter the adapter to measure
     * @return the width required for the content of the list adapter
     */
    public static int measureContentWidth(@NonNull Context ctx, @NonNull ListAdapter adapter) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < adapter.getCount(); i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(ctx);
            }

            itemView = adapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);

            final int itemWidth = itemView.getMeasuredWidth();

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        return maxWidth;
    }
}
