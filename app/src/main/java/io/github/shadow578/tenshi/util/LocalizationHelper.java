package io.github.shadow578.tenshi.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.mal.model.type.BroadcastStatus;
import io.github.shadow578.tenshi.mal.model.type.DayOfWeek;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.mal.model.type.MediaType;
import io.github.shadow578.tenshi.mal.model.type.RelationType;
import io.github.shadow578.tenshi.mal.model.type.Source;
import io.github.shadow578.tenshi.mal.model.type.YearSeason;

import static io.github.shadow578.tenshi.lang.LanguageUtils.*;

public class LocalizationHelper {
    @NonNull
    public static String localizeMediaType(@Nullable MediaType mediaType, @NonNull Context ctx) {
        if (isNull(mediaType))
            return ctx.getString(R.string.shared_unknown);

        switch (mediaType) {
            case TV:
                return ctx.getString(R.string.media_type_tv);
            case OVA:
                return ctx.getString(R.string.media_type_ova);
            case ONA:
                return ctx.getString(R.string.media_type_ona);
            case Movie:
                return ctx.getString(R.string.media_type_movie);
            case Special:
                return ctx.getString(R.string.media_type_special);
            case Music:
                return ctx.getString(R.string.media_type_music);
            case Manga:
                return ctx.getString(R.string.media_type_manga);
            case OneShot:
                return ctx.getString(R.string.media_type_one_shot);
            case Manhwa:
                return ctx.getString(R.string.media_type_manhwa);
            case Manhua:
                return ctx.getString(R.string.media_type_manhua);
            case Novel:
                return ctx.getString(R.string.media_type_novel);
            case LightNovel:
                return ctx.getString(R.string.media_type_light_novel);
            case Doujinshi:
                return ctx.getString(R.string.media_type_doujinshi);
            case Unknown:
                return ctx.getString(R.string.shared_unknown);
            default:
                return EnumHelper.valueOf(mediaType);
        }
    }

    @NonNull
    public static String localizeBroadcastStatus(@Nullable BroadcastStatus status, @NonNull Context ctx) {
        if (isNull(status))
            return ctx.getString(R.string.shared_unknown);

        switch (status) {
            case CurrentlyAiring:
                return ctx.getString(R.string.broadcast_status_airing);
            case Finished:
            case FinishedAiring:
                return ctx.getString(R.string.broadcast_status_finished);
            case NotYetAired:
                return ctx.getString(R.string.broadcast_status_not_yet_aired);
            case CurrentlyPublishing:
                return ctx.getString(R.string.broadcast_status_publishing);
            case OnHiatus:
                return ctx.getString(R.string.broadcast_status_on_hiatus);
            case Discontinued:
                return ctx.getString(R.string.broadcast_status_discontinued);
            default:
                return EnumHelper.valueOf(status);
        }
    }

    @NonNull
    public static String localizeLibraryStatus(@Nullable LibraryEntryStatus status, @NonNull Context ctx) {
        if (isNull(status))
            return ctx.getString(R.string.shared_unknown);

        switch (status) {
            case Watching:
                return ctx.getString(R.string.list_status_watching);
            case Completed:
                return ctx.getString(R.string.list_status_completed);
            case OnHold:
                return ctx.getString(R.string.list_status_on_hold);
            case Dropped:
                return ctx.getString(R.string.list_status_dropped);
            case PlanToWatch:
                return ctx.getString(R.string.list_status_plan_to_watch);
            default:
                return elvis(EnumHelper.valueOf(status), "");
        }
    }

    @NonNull
    public static String localizeSeason(@Nullable YearSeason season, @NonNull Context ctx) {
        if (isNull(season))
            return ctx.getString(R.string.shared_unknown);

        switch (season) {
            case Winter:
                return ctx.getString(R.string.season_winter);
            case Spring:
                return ctx.getString(R.string.season_spring);
            case Summer:
                return ctx.getString(R.string.season_summer);
            case Fall:
                return ctx.getString(R.string.season_fall);
            default:
                return EnumHelper.valueOf(season);
        }
    }

    @NonNull
    public static String localizeSource(@Nullable Source source, @NonNull Context ctx) {
        if (isNull(source))
            return ctx.getString(R.string.shared_unknown);

        switch (source) {
            case Original:
                return ctx.getString(R.string.source_original);
            case Manga:
                return ctx.getString(R.string.source_manga);
            case Manhua:
                return ctx.getString(R.string.source_manhua);
            case LightNovel:
                return ctx.getString(R.string.source_light_novel);
            case VisualNovel:
                return ctx.getString(R.string.source_visual_novel);
            case Game:
                return ctx.getString(R.string.source_game);
            case WebManga:
                return ctx.getString(R.string.source_web_manga);
            case Music:
                return ctx.getString(R.string.source_music);
            case FourKoma:
                return ctx.getString(R.string.source_4_koma_manga);
            // TODO include the other sources too
            case Other:
                return ctx.getString(R.string.shared_unknown);
            default:
                return elvis(EnumHelper.valueOf(source), "");
        }
    }

    @NonNull
    public static String localizeScore(int score, @NonNull Context ctx) {
        ArrayList<String> map = listOf(
                ctx.getString(R.string.shared_no_value),
                ctx.getString(R.string.score_horrible_plus),
                ctx.getString(R.string.score_horrible),
                ctx.getString(R.string.score_very_bad),
                ctx.getString(R.string.score_bad),
                ctx.getString(R.string.score_average),
                ctx.getString(R.string.score_fine),
                ctx.getString(R.string.score_good),
                ctx.getString(R.string.score_very_good),
                ctx.getString(R.string.score_great),
                ctx.getString(R.string.score_masterpiece)
        );

        if (score > map.size() || score < 0)
            return ctx.getString(R.string.shared_no_value);
        return map.get(score);
    }

    @NonNull
    public static String localizeWeekday(@Nullable DayOfWeek weekday, @NonNull Context ctx) {
        if (isNull(weekday))
            return ctx.getString(R.string.shared_unknown);

        switch (weekday) {
            case Monday:
                return ctx.getString(R.string.weekday_monday);
            case Tuesday:
                return ctx.getString(R.string.weekday_tuesday);
            case Wednesday:
                return ctx.getString(R.string.weekday_wednesday);
            case Thursday:
                return ctx.getString(R.string.weekday_thursday);
            case Friday:
                return ctx.getString(R.string.weekday_friday);
            case Saturday:
                return ctx.getString(R.string.weekday_saturday);
            case Sunday:
                return ctx.getString(R.string.weekday_sunday);
            default:
                return EnumHelper.valueOf(weekday);
        }
    }

    @NonNull
    public static String localizeRelation(@Nullable RelationType relation, @NonNull Context ctx) {
        if(isNull(relation))
            return ctx.getString(R.string.shared_unknown);

        switch (relation)
        {
            case Prequel: return ctx.getString(R.string.relation_prequel);
            case Sequel: return ctx.getString(R.string.relation_sequel);
            case Summary: return ctx.getString(R.string.relation_summary);
            case AlternativeVersion: return ctx.getString(R.string.relation_alternative_version);
            case AlternativeSetting: return ctx.getString(R.string.relation_alternative_setting);
            case SpinOff: return ctx.getString(R.string.relation_spin_off);
            case SideStory: return ctx.getString(R.string.relation_side_story);
            case ParentStory: return ctx.getString(R.string.relation_parent_story);
            case FullStory: return ctx.getString(R.string.relation_full_story);
            case Adaptation: return ctx.getString(R.string.relation_adaption);
            case Character: return ctx.getString(R.string.relation_character);
            case Other: return ctx.getString(R.string.relation_other);
            default: return EnumHelper.valueOf(relation);
        }
    }
}
