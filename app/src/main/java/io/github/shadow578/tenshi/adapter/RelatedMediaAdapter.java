package io.github.shadow578.tenshi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.RecyclerAnimeSmallBinding;
import io.github.shadow578.tenshi.extensionslib.lang.BiConsumer;
import io.github.shadow578.tenshi.extensionslib.lang.Consumer;
import io.github.shadow578.tenshi.mal.model.RelatedMedia;
import io.github.shadow578.tenshi.mal.model.type.TitleDisplayMode;
import io.github.shadow578.tenshi.util.GlideHelper;
import io.github.shadow578.tenshi.util.LocalizationHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.*;

/**
 * recycler view adapter for a list of {@link RelatedMedia}
 */
public class RelatedMediaAdapter extends RecyclerView.Adapter<RelatedMediaAdapter.Holder> {
    @NonNull
    private final Context ctx;
    @NonNull
    private final List<RelatedMedia> anime;
    @NonNull
    private final BiConsumer<View, RelatedMedia> onClickListener;

    @Nullable
    private Consumer<Integer> endListener;

    /**
     * create the adapter
     * @param _c the context to work in
     * @param _anime the list of related media to display
     * @param _clickListener the click listener, called when one of the related media is clicked
     */
    public RelatedMediaAdapter(@NonNull Context _c, @NonNull List<RelatedMedia> _anime, @NonNull BiConsumer<View, RelatedMedia> _clickListener) {
        ctx = _c;
        anime = _anime;
        onClickListener = _clickListener;
    }

    /**
     * set the end listener that is called when the end of the list is reached
     * @param l the end listener
     */
    @SuppressWarnings({"unused", "RedundantSuppression"})
    public void setEndListener(@Nullable Consumer<Integer> l)
    {
        endListener = l;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_anime_small, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        final RelatedMedia anime = this.anime.get(pos);
        final RecyclerAnimeSmallBinding b = holder.binding;
        final String unknown = ctx.getString(R.string.shared_unknown);

        // poster
        with(anime.relatedAnime.poster, p
                ->GlideHelper.glide(ctx, p.mediumUrl).into(b.animeMainPoster));

        // title
        final TitleDisplayMode titleMode = TenshiPrefs.getEnum(TenshiPrefs.Key.TitleDisplayMode, TitleDisplayMode.class, TitleDisplayMode.Canonical);
        b.animeTitle.setText(elvisEmpty(anime.relatedAnime.getDisplayTitle(titleMode), unknown));

        // relation
        b.animeRelationType.setText(elvisEmpty(LocalizationHelper.localizeRelation(anime.relationType, ctx), unknown));

        // setup onclick listener
        holder.itemView.setOnClickListener(view -> onClickListener.invoke(view, anime));

        // call bottom reached listener if we're at the bottom
        if(pos == this.anime.size() - 2 && endListener != null)
            endListener.invoke(pos);
    }

    @Override
    public int getItemCount() {
        return anime.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public final RecyclerAnimeSmallBinding binding;

        public Holder(@NonNull View v) {
            super(v);
            // bind view
            binding = RecyclerAnimeSmallBinding.bind(v);
            binding.animeRelationType.setVisibility(View.VISIBLE);

            //enable clipToOutline on poster
            binding.animeMainPoster.setClipToOutline(true);
        }
    }
}
