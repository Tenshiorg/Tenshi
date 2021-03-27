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
import io.github.shadow578.tenshi.mal.model.AnimeRankingItem;
import io.github.shadow578.tenshi.util.GlideHelper;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.*;

/**
 * recycler view adapter for a list of {@link AnimeRankingItem}
 */
public class SeasonalAnimeAdapter extends RecyclerView.Adapter<SeasonalAnimeAdapter.Holder> {
    @NonNull
    private final Context ctx;
    @NonNull
    private final List<AnimeRankingItem> anime;
    @NonNull
    private final BiConsumer<View, AnimeRankingItem> onClickListener;

    @Nullable
    private Consumer<Integer> endListener;

    /**
     * create the adapter
     *
     * @param _c             the context to work in
     * @param _anime         the list of ranked anime to display
     * @param _clickListener the click listener, called when one of the items is clicked
     */
    public SeasonalAnimeAdapter(@NonNull Context _c, @NonNull List<AnimeRankingItem> _anime, @NonNull BiConsumer<View, AnimeRankingItem> _clickListener) {
        ctx = _c;
        anime = _anime;
        onClickListener = _clickListener;
    }

    /**
     * set the end listener that is called when the end of the list is reached
     *
     * @param l the end listener
     */
    @SuppressWarnings({"unused", "RedundantSuppression"})
    public void setEndListener(@Nullable Consumer<Integer> l) {
        endListener = l;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_anime_small, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        final AnimeRankingItem anime = this.anime.get(pos);
        final RecyclerAnimeSmallBinding b = h.binding;
        final String unknown = ctx.getString(R.string.shared_unknown);

        // poster
        with(anime.anime.poster, p
                -> GlideHelper.glide(ctx, p.mediumUrl).into(b.animeMainPoster));

        // title
        b.animeTitle.setText(elvisEmpty(anime.anime.title, unknown));

        // setup onclick listener
        h.itemView.setOnClickListener(view -> onClickListener.invoke(b.animeMainPoster, anime));

        // call bottom reached listener if we're at the bottom
        if (pos == this.anime.size() - 2 && endListener != null)
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

            //get binding
            binding = RecyclerAnimeSmallBinding.bind(v);
            binding.animeRelationType.setVisibility(View.GONE);

            //enable clipToOutline on poster
            binding.animeMainPoster.setClipToOutline(true);
        }
    }
}
