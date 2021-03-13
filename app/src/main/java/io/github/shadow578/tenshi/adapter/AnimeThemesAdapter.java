package io.github.shadow578.tenshi.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.RecyclerThemeBinding;
import io.github.shadow578.tenshi.mal.model.Theme;
import static io.github.shadow578.tenshi.lang.LanguageUtils.*;

/**
 * recycler view adapter for a list of {@link Theme}
 */
public class AnimeThemesAdapter extends RecyclerView.Adapter<AnimeThemesAdapter.Holder> {
    @NonNull
    private final Context ctx;
    @NonNull
    private final List<Theme> themes;

    /**
     * create the adapter
     * @param _c the context to work in
     * @param _themes the list of themes to display
     */
    public AnimeThemesAdapter(@NonNull Context _c, @NonNull List<Theme> _themes) {
        ctx = _c;
        themes = _themes;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_theme, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int pos) {
        final Theme theme = themes.get(pos);
        final RecyclerThemeBinding b = holder.binding;

        // theme
        with(theme.text, b.theme::setText);

        // set click and longclick listeners
        // click = search theme in youtube
        // long click = copy theme to clipboard
        holder.itemView.setOnClickListener(view -> {
            // build query to search for theme on youtube
            String query = theme.text.replace(" ", "+");
            if (query.startsWith("#"))
                query = query.replaceFirst("#", "");

            // open youtube
            //TODO: hardcoded uri
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("https://www.youtube.com/results?search_query=" + query));
            ctx.startActivity(intent);
        });
        holder.itemView.setOnLongClickListener(view -> {
            ClipboardManager clipMgr = cast(ctx.getSystemService(Context.CLIPBOARD_SERVICE));
            with(clipMgr, clip -> {
                ClipData data = ClipData.newPlainText("theme", theme.text);
                clip.setPrimaryClip(data);
                Toast.makeText(ctx, R.string.shared_toast_copied_to_clip, Toast.LENGTH_SHORT).show();
            });
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public final RecyclerThemeBinding binding;

        public Holder(@NonNull View v) {
            super(v);
            // bind view
            binding = RecyclerThemeBinding.bind(v);
        }
    }
}
