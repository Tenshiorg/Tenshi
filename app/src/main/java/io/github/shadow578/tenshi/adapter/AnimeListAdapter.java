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
import io.github.shadow578.tenshi.databinding.RecyclerAnimeBigBinding;
import io.github.shadow578.tenshi.lang.BiConsumer;
import io.github.shadow578.tenshi.lang.Consumer;
import io.github.shadow578.tenshi.mal.model.AnimeListItem;
import io.github.shadow578.tenshi.util.GlideHelper;
import io.github.shadow578.tenshi.util.LocalizationHelper;

import static io.github.shadow578.tenshi.lang.LanguageUtils.concat;
import static io.github.shadow578.tenshi.lang.LanguageUtils.elvis;
import static io.github.shadow578.tenshi.lang.LanguageUtils.elvisEmpty;
import static io.github.shadow578.tenshi.lang.LanguageUtils.fmt;
import static io.github.shadow578.tenshi.lang.LanguageUtils.join;
import static io.github.shadow578.tenshi.lang.LanguageUtils.str;
import static io.github.shadow578.tenshi.lang.LanguageUtils.with;
import static io.github.shadow578.tenshi.lang.LanguageUtils.withStr;

/**
 * recycler view adapter for a list of {@link AnimeListItem}
 */
public class AnimeListAdapter extends RecyclerView.Adapter<AnimeListAdapter.Holder> {
    @NonNull
    private final Context ctx;
    @NonNull
    private final List<AnimeListItem> anime;
    @NonNull
    private final BiConsumer<View, AnimeListItem> onClickListener;

    @Nullable
    private Consumer<Integer> endListener;

    /**
     * initialize the adapter
     *
     * @param _c             the context to work in
     * @param _anime         the anime to display
     * @param _clickListener a listener for click on anime cards
     */
    public AnimeListAdapter(@NonNull Context _c, @NonNull List<AnimeListItem> _anime, @NonNull BiConsumer<View, AnimeListItem> _clickListener) {
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_anime_big, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        final AnimeListItem anime = this.anime.get(pos);
        final RecyclerAnimeBigBinding b = holder.binding;
        final String unknown = ctx.getString(R.string.shared_unknown);

        // poster
        with(anime.anime.poster, p
                -> GlideHelper.glide(ctx, p.mediumUrl).into(b.animeMainPoster));

        // title
        b.animeTitle.setText(elvisEmpty(anime.anime.title, unknown));

        // media status
        //TODO: hardcoded string, use resource
        final int episodes = elvis(anime.anime.episodesCount, 0);
        if (anime.anime.mediaType != null)
            b.animeStatus.setText(concat(LocalizationHelper.localizeMediaType(anime.anime.mediaType, ctx), " (" + fmt(ctx, R.string.shared_num_episodes_fmt, str(episodes)) + ")"));
        else
            b.animeStatus.setText(unknown);


        // season
        with(anime.anime.startSeason, p -> b.animeSeason.setText(join(" ", LocalizationHelper.localizeSeason(p.season, ctx), str(p.year))));

        // score
        withStr(anime.anime.meanScore, unknown, b.animeScore::setText);

        // setup onclick listener
        holder.itemView.setOnClickListener(view -> onClickListener.invoke(view, anime));

        // call bottom reached listener if we're at the bottom
        if (pos == this.anime.size() - 2 && endListener != null)
            endListener.invoke(pos);
    }

    @Override
    public int getItemCount() {
        return anime.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public final RecyclerAnimeBigBinding binding;

        public Holder(@NonNull View v) {
            super(v);

            // get binding
            binding = RecyclerAnimeBigBinding.bind(v);

            //enable clipToOutline on poster
            binding.animeMainPoster.setClipToOutline(true);
        }
    }
}
