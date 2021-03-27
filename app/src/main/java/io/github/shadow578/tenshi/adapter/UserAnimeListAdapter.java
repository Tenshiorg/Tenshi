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
import io.github.shadow578.tenshi.databinding.RecyclerAnimeListItemBinding;
import io.github.shadow578.tenshi.extensionslib.lang.BiConsumer;
import io.github.shadow578.tenshi.extensionslib.lang.Consumer;
import io.github.shadow578.tenshi.mal.model.UserLibraryEntry;
import io.github.shadow578.tenshi.util.GlideHelper;
import io.github.shadow578.tenshi.util.LocalizationHelper;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.*;

/**
 * recycler view adapter for a list of {@link UserLibraryEntry}
 */
public class UserAnimeListAdapter extends RecyclerView.Adapter<UserAnimeListAdapter.Holder> {
    @NonNull
    private final Context ctx;
    @NonNull
    private final List<UserLibraryEntry> anime;
    @NonNull
    private final BiConsumer<View, UserLibraryEntry> onClickListener;

    @Nullable
    private Consumer<Integer> endListener;

    /**
     * create the adapter
     *
     * @param _c             the context to work in
     * @param _anime         the list of library entries to display
     * @param _clickListener click listener for the items
     */
    public UserAnimeListAdapter(@NonNull Context _c, @NonNull List<UserLibraryEntry> _anime, @NonNull BiConsumer<View, UserLibraryEntry> _clickListener) {
        ctx = _c;
        anime = _anime;
        onClickListener = _clickListener;
    }

    /**
     * set the end listener that is called when the end of the list is reached
     *
     * @param l the end listener
     */
    public void setEndListener(@Nullable Consumer<Integer> l) {
        endListener = l;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_anime_list_item, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        final UserLibraryEntry anime = this.anime.get(pos);
        final RecyclerAnimeListItemBinding b = holder.binding;
        final String unknown = ctx.getString(R.string.shared_unknown);
        final String noValue = ctx.getString(R.string.shared_no_value);

        // poster
        with(anime.anime.poster, p
                -> GlideHelper.glide(ctx, p.mediumUrl).into(b.animeMainPoster));

        // title
        b.animeTitle.setText(elvisEmpty(anime.anime.title, unknown));

        // score
        b.animeScore.setText(elvis(withRet(anime.libraryStatus, p -> p.score != 0 ? fmt(p.score) : null), noValue));

        // watch progress
        int watchedEp = elvis(withRet(anime.libraryStatus, p -> p.watchedEpisodes), 0);
        int totalEp = elvis(anime.anime.episodesCount, 10);
        b.animeEpisodesProgressLabel.setText(concat(fmt(watchedEp), "/", fmt(totalEp)));
        b.animeEpisodesProgressBar.setProgress(watchedEp);
        b.animeEpisodesProgressBar.setMax(totalEp);

        // media status
        b.animeStatus.setText(LocalizationHelper.localizeBroadcastStatus(anime.anime.broadcastStatus, ctx));

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
        public final RecyclerAnimeListItemBinding binding;

        public Holder(@NonNull View v) {
            super(v);
            // bind view
            binding = RecyclerAnimeListItemBinding.bind(v);

            //enable clipToOutline on poster
            binding.animeMainPoster.setClipToOutline(true);
        }
    }
}
