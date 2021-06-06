package io.github.shadow578.tenshi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.RecyclerTraceResultBinding;
import io.github.shadow578.tenshi.extensionslib.lang.BiConsumer;
import io.github.shadow578.tenshi.trace.model.AnilistTitles;
import io.github.shadow578.tenshi.trace.model.TraceResult;
import io.github.shadow578.tenshi.util.GlideHelper;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.elvisEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.withStr;

/**
 * recycler view adapter for a list of {@link TraceResult}
 */
public class TraceResultsAdapter extends RecyclerView.Adapter<TraceResultsAdapter.Holder> {

    @NonNull
    private final Context ctx;
    @NonNull
    private final List<TraceResult> results;
    @NonNull
    private final BiConsumer<View, TraceResult> onClickListener;

    /**
     * initialize the adapter
     *
     * @param _c             the context to work in
     * @param _results       trace results to show
     * @param _clickListener a listener for click on result cards
     */
    public TraceResultsAdapter(@NonNull Context _c, @NonNull List<TraceResult> _results, @NonNull BiConsumer<View, TraceResult> _clickListener) {
        ctx = _c;
        results = _results;
        onClickListener = _clickListener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_trace_result, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        final TraceResult result = results.get(pos);
        final RecyclerTraceResultBinding b = holder.binding;
        final String unknown = ctx.getString(R.string.shared_unknown);


        // image preview
        with(result.previewImageUrl, img
                -> GlideHelper.glide(ctx, img).into(b.imagePreview));

        // title
        // use english, fallback to romanji, then native. only if all titles are not set, use "unknown"
        if (notNull(result.anilistInfo) && notNull(result.anilistInfo.titles)) {
            final AnilistTitles t = result.anilistInfo.titles;
            b.title.setText(elvisEmpty(t.englishTitle, elvisEmpty(t.romanjiTitle, elvisEmpty(t.nativeTitle, unknown))));
        } else
            b.title.setText(unknown);

        // episode number
        // TODO hardcoded string, use resource
        withStr(result.episode, unknown, ep
                -> b.episode.setText("Episode " + ep));

        // scene span
        // TODO convert seconds to mm:ss (or hh:mm:ss if that long)
        // TODO hardcoded string, use resource
        final String sceneStart = fmt("%.0f",result.sceneStartSeconds);
        final String sceneEnd = fmt("%.0f",result.sceneEndSeconds);
        b.sceneSpan.setText(sceneStart + " - " + sceneEnd);

        // match confidence
        b.matchConfidence.setText(fmt("%.0f%%", result.similarity * 100));

        // setup click listener
        holder.itemView.setOnClickListener(view -> onClickListener.invoke(view, result));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public final RecyclerTraceResultBinding binding;

        public Holder(@NonNull View v) {
            super(v);

            // get binding
            binding = RecyclerTraceResultBinding.bind(v);
        }
    }
}
