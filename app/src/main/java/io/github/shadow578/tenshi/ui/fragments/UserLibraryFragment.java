package io.github.shadow578.tenshi.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.databinding.FragmentAnimelistBinding;
import io.github.shadow578.tenshi.mal.model.type.LibrarySortMode;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.util.LocalizationHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;

import static io.github.shadow578.tenshi.lang.LanguageUtils.isNull;

/**
 * fragment for viewing the user library, with categories in tabs
 */
public class UserLibraryFragment extends TenshiFragment {

    // use WeakReference so we don't keep any fragment that viewpager no longer used from being garbage collected
    private final HashMap<Integer, WeakReference<UserLibraryCategoryFragment>> fragmentsMap = new HashMap<>();

    private LibrarySortMode sortMode;
    private LibraryEntryStatus[] tabCategories;
    private String[] tabTitles;
    private FragmentAnimelistBinding b;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load default sort mode from prefs
        sortMode = TenshiPrefs.getEnum(TenshiPrefs.Key.LibrarySortMode, LibrarySortMode.class, LibrarySortMode.anime_title);

        // load all categories
        tabCategories = LibraryEntryStatus.values();

        // ... and localized tab titles
        ArrayList<String> localizedNames = new ArrayList<>();
        for (LibraryEntryStatus cat : tabCategories)
            localizedNames.add(LocalizationHelper.localizeLibraryStatus(cat, requireContext()));
        tabTitles = localizedNames.toArray(new String[0]);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentAnimelistBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // create and set adapter for view pager
        AnimeListCategoryFragmentAdapter fragmentAdapter = new AnimeListCategoryFragmentAdapter(requireActivity(), tabCategories);
        b.categoryViewPager.setAdapter(fragmentAdapter);

        // link tab layout and view pager
        // such that tab titles show correctly
        new TabLayoutMediator(b.categoryTabs, b.categoryViewPager, (tab, pos) -> tab.setText(tabTitles[pos])).attach();

        // register to page change listener to update the sort mode of the currently visible fragment
        b.categoryViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateSortModeInFragment();

                // save last tab to prefs
                TenshiPrefs.setEnum(TenshiPrefs.Key.LastLibraryCategory, tabCategories[position]);
            }
        });

        // switch to the previous tab (saved in prefs)
        final LibraryEntryStatus lastCategory = TenshiPrefs.getEnum(TenshiPrefs.Key.LastLibraryCategory, LibraryEntryStatus.class, tabCategories[0]);
        int targetTabPos = 0;
        for (int i = 0; i < tabCategories.length; i++)
            if (tabCategories[i].equals(lastCategory)) {
                targetTabPos = i;
                break;
            }

        // switch to previous tab after 100 seconds (delayed because wont work otherwise
        int fTargetTabPos = targetTabPos;
        b.categoryViewPager.postDelayed(() -> b.categoryViewPager.setCurrentItem(fTargetTabPos, true), 100);
            
        // update the sort mode when a menu item is selected
        b.sortModeFab.addOnMenuItemClickListener((fab, textView, id) -> updateSortMode(id));

        // change fab icon to X when open (rotates 40 deg, so we use a + instead)
        b.sortModeFab.addOnStateChangeListener(open
                -> b.sortModeFab.getMainFab().setImageResource(open ? R.drawable.ic_action_add : R.drawable.ic_round_filter_list_24));
    }

    /**
     * update the sort mode based on the selected ID
     * @param menuId the id of the view that was selected
     */
    private void updateSortMode(int menuId) {
        // figure out what sort mode was selected
        LibrarySortMode newSort = sortMode;
        if (menuId == R.id.sort_mode_by_title)
            newSort = LibrarySortMode.anime_title;
        else if (menuId == R.id.sort_mode_by_score)
            newSort = LibrarySortMode.list_score;
        if (menuId == R.id.sort_mode_by_updated)
            newSort = LibrarySortMode.list_updated_at;

        // update sort mode if changed
        if (!sortMode.equals(newSort)) {
            sortMode = newSort;

            // update in fragment
            updateSortModeInFragment();

            // save to prefs
            TenshiPrefs.setEnum(TenshiPrefs.Key.LibrarySortMode, newSort);
        }
    }

    /**
     * update the sort mode in the fragment that is currently visible
     */
    private void updateSortModeInFragment() {
        // get the currently selected fragment's position
        final int currentPos = b.categoryViewPager.getCurrentItem();

        // check if the fragment with that position exists, abort if not
        if (!fragmentsMap.containsKey(currentPos)) {
            Log.e("Tenshi", "cannot find fragment in map: pos=" + currentPos);
            return;
        }

        // get the current fragment
        WeakReference<UserLibraryCategoryFragment> fRef = fragmentsMap.get(currentPos);

        // check the reference is still good
        if (isNull(fRef) || isNull(fRef.get())) {
            Log.e("Tenshi", "weak reference to fragment was null: pos=" + currentPos);
            return;
        }

        // call setSortMode on the fragment
        fRef.get().setSortMode(sortMode);
    }

    public class AnimeListCategoryFragmentAdapter extends FragmentStateAdapter {
        private final LibraryEntryStatus[] categories;

        public AnimeListCategoryFragmentAdapter(@NonNull FragmentActivity fragmentActivity, @NonNull LibraryEntryStatus[] categoriesOrdered) {
            super(fragmentActivity);
            categories = categoriesOrdered;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // init fragment with category and correct sorting mode
            UserLibraryCategoryFragment f = new UserLibraryCategoryFragment(categories[position], sortMode);

            // add to fragments list so we can access it later
            fragmentsMap.remove(position);
            fragmentsMap.put(position, new WeakReference<>(f));

            // add scroll listener
            f.setOnScrollChangeListener((dx, dy) -> {
                if (dy > 0) {
                    // we're going down, hide fab and selection fabs
                    b.sortModeFab.hide();
                } else if (dy < 0) {
                    //we're going up, show fab
                    b.sortModeFab.show();
                }
            });
            return f;
        }

        @Override
        public int getItemCount() {
            return categories.length;
        }
    }
}
