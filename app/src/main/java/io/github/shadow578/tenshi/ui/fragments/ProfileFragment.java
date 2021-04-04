package io.github.shadow578.tenshi.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.snackbar.Snackbar;

import app.futured.donut.DonutSection;
import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.databinding.FragmentProfileBinding;
import io.github.shadow578.tenshi.mal.MalApiHelper;
import io.github.shadow578.tenshi.mal.model.User;
import io.github.shadow578.tenshi.mal.model.UserStatistics;
import io.github.shadow578.tenshi.ui.FullscreenImageActivity;
import io.github.shadow578.tenshi.util.DateHelper;
import io.github.shadow578.tenshi.util.GlideHelper;
import io.github.shadow578.tenshi.util.TenshiPrefs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.async;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.elvis;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.elvisEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.listOf;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;

/**
 * fragment for viewing the current user's profile and stats
 */
public class ProfileFragment extends TenshiFragment {

    private static final String REQUEST_FIELDS = MalApiHelper.getQueryableFields(User.class);

    private FragmentProfileBinding b;
    private User user;
    private UserStatistics userAnimeStatistics;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentProfileBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // update views if user was loaded from db already
        if (notNull(user))
            populateViewData();

        // fetch user info from MAL
        fetchUser();
    }

    /**
     * get the user data from MAL
     */
    private void fetchUser() {
        // load from db
        async(() -> {
            // get user ID from prefs
            int userId = TenshiPrefs.getInt(TenshiPrefs.Key.UserID, -1);
            if (userId != -1)
                return TenshiApp.getDB().userDB().getUserById(userId);
            else
                return null;
        }, u -> {
            // populate views only if not already loaded from MAL
            if (notNull(u)) {
                // update access
                async(() -> TenshiApp.getDB().accessDB().updateForUser(u.userID));

                // update ui
                if (isNull(user)) {
                    user = u;
                    userAnimeStatistics = u.statistics;
                    populateViewData();
                }
            }
        });

        // request from MAL
        TenshiApp.getMal().getCurrentUser(REQUEST_FIELDS)
                .enqueue(new Callback<User>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<User> call, Response<User> response) {
                        // callback to TenshiApp global reauth
                        if (isAdded())
                            TenshiApp.malReauthCallback(requireActivity(), response);

                        if (response.isSuccessful() && notNull(response.body())) {
                            // get user from response
                            user = response.body();
                            userAnimeStatistics = user.statistics;

                            // update views
                            populateViewData();

                            // insert into db
                            async(() -> TenshiApp.getDB().userDB().insertOrUpdateUser(user));

                            // save user ID to prefs
                            TenshiPrefs.setInt(TenshiPrefs.Key.UserID, user.userID);
                        } else if (response.code() == 401 && isAdded())
                            Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("Tenshi", t.toString());
                        if (isAdded())
                            Snackbar.make(b.getRoot(), R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * populate the views with data
     */
    private void populateViewData() {
        final String unknown = getString(R.string.shared_unknown);

        // abort if no user loaded
        if (isNull(user))
            return;

        // load profile picture
        GlideHelper.glide(requireContext(), user.profilePictureUrl, R.drawable.ic_round_account_circle_24)
                .circleCrop()
                .into(b.userProfilePicture);

        // open fullscreen picture when clicking pfp
        b.userProfilePicture.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), FullscreenImageActivity.class);
            ActivityOptionsCompat opt = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    b.userProfilePicture,
                    b.userProfilePicture.getTransitionName());
            i.putExtra(FullscreenImageActivity.EXTRA_IMAGE_URL, user.profilePictureUrl);
            startActivity(i, opt.toBundle());
        });

        // set username text
        b.userName.setText(elvisEmpty(user.name, unknown));

        // set location, make invisible if unknown
        setTextOrInvisible(b.userLocation, user.location);

        // birthday
        setTextOrInvisible(b.userBirthday, DateHelper.format(user.birthday, null));

        // joined at, convert to local date
        //setTextOrInvisible(b.joinedAt, LocalDate.parse(user.joined_at, DateTimeFormatter.ISO_DATE_TIME).toString());
        setTextOrInvisible(b.userJoinedDate, DateHelper.format(user.joinedAt, null));

        // donut category descriptions
        b.donutDescWatching.setText(fmt(requireContext(), R.string.profile_donut_desc_watching_fmt, fmt(userAnimeStatistics.libraryWatchingCount)));
        b.donutDescCompleted.setText(fmt(requireContext(), R.string.profile_donut_desc_completed_fmt, fmt(userAnimeStatistics.libraryCompletedCount)));
        b.donutDescOnHold.setText(fmt(requireContext(), R.string.profile_donut_desc_on_hold_fmt, fmt(userAnimeStatistics.libraryOnHoldCount)));
        b.donutDescDropped.setText(fmt(requireContext(), R.string.profile_donut_desc_dropped_fmt, fmt(userAnimeStatistics.libraryDroppedCount)));
        b.donutDescPlanToWatch.setText(fmt(requireContext(), R.string.profile_donut_desc_plan_to_watch_fmt, fmt(userAnimeStatistics.libraryPlanToWatchCount)));

        // donut center text
        b.donutTotalEntries.setText(fmt(requireContext(), R.string.profile_total_entries_fmt, fmt(userAnimeStatistics.libraryTotalCount)));

        // stats below donut
        final double num_days = elvis(userAnimeStatistics.totalDaysWasted, 0.0);
        b.statDaysWasted.setText(fmt(requireContext(), R.string.profile_days_wasted_fmt, fmt(num_days)));
        b.statTotalEpisodesWatched.setText(fmt(requireContext(), R.string.profile_num_episodes_fmt, fmt(userAnimeStatistics.totalEpisodesWatched)));

        // donut
        updateWatchStatsDonut();

        // setup "view on MAL" button
        b.viewOnMalBtn.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://myanimelist.net/profile/" + user.name));
            startActivity(i);
        });

        // hide loading indicator
        b.loadingIndicator.hide();
    }

    /**
     * update the stats donut
     */
    private void updateWatchStatsDonut() {
        // cancel if not stats are available
        if (isNull(userAnimeStatistics))
            return;

        // prepare donut sections
        final Context c = requireContext();
        DonutSection watching = new DonutSection(
                "Watching",
                c.getColor(R.color.profile_donut_watching),
                elvis(userAnimeStatistics.libraryWatchingCount, 0));
        DonutSection completed = new DonutSection(
                "Completed",
                c.getColor(R.color.profile_donut_completed),
                elvis(userAnimeStatistics.libraryCompletedCount, 0));
        DonutSection onHold = new DonutSection(
                "On Hold",
                c.getColor(R.color.profile_donut_on_hold),
                elvis(userAnimeStatistics.libraryOnHoldCount, 0));
        DonutSection dropped = new DonutSection(
                "Dropped",
                c.getColor(R.color.profile_donut_dropped),
                elvis(userAnimeStatistics.libraryDroppedCount, 0));
        DonutSection ptw = new DonutSection(
                "Plan to Watch",
                c.getColor(R.color.profile_donut_plan_to_watch),
                elvis(userAnimeStatistics.libraryPlanToWatchCount, 0));

        // update chart
        b.userStatsDonut.setCap(0);
        b.userStatsDonut.submitData(listOf(ptw, dropped, onHold, completed, watching));
    }

    /**
     * set the text of a view, or, if the string is null, make it invisible
     */
    private void setTextOrInvisible(@NonNull TextView v, @Nullable String txt) {
        if (nullOrEmpty(txt))
            v.setVisibility(View.GONE);
        else {
            v.setVisibility(View.VISIBLE);
            v.setText(txt);
        }
    }
}
