package io.github.shadow578.tenshi.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.mal.model.Image;
import io.github.shadow578.tenshi.mal.model.type.BroadcastStatus;
import io.github.shadow578.tenshi.mal.model.type.ContentRating;
import io.github.shadow578.tenshi.mal.model.type.DayOfWeek;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;
import io.github.shadow578.tenshi.mal.model.type.MediaType;
import io.github.shadow578.tenshi.mal.model.type.NSFWRating;
import io.github.shadow578.tenshi.mal.model.type.RelationType;
import io.github.shadow578.tenshi.mal.model.type.Source;
import io.github.shadow578.tenshi.mal.model.type.YearSeason;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;

/**
 * type converters for the room DB
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class TenshiTypeConverters {
    /**
     * gson from TenshiApp
     */
    private final Gson gson = TenshiApp.getGson();

    // region ZonedDateTime
    @TypeConverter
    @Nullable
    public String serializeZonedDateTime(@Nullable ZonedDateTime obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public ZonedDateTime deserializeZonedDateTime(@NonNull String val) {
        return gson.fromJson(val, ZonedDateTime.class);
    }
    //endregion

    // region LocalDate
    @TypeConverter
    @Nullable
    public String serializeLocalDate(@Nullable LocalDate obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public LocalDate deserializeLocalDate(@NonNull String val) {
        return gson.fromJson(val, LocalDate.class);
    }
    //endregion

    // region LocalTime
    @TypeConverter
    @Nullable
    public String serializeLocalTime(@Nullable LocalTime obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public LocalTime deserializeLocalTime(@NonNull String val) {
        return gson.fromJson(val, LocalTime.class);
    }
    //endregion

    // region List<Image>
    @TypeConverter
    @Nullable
    public String serializeImageList(@Nullable List<Image> obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public List<Image> deserializeImageList(@NonNull String val) {
        return gson.fromJson(val, new TypeToken<List<Image>>() {
        }.getType());
    }
    //endregion

    // region List<String>
    @TypeConverter
    @Nullable
    public String serializeStringList(@Nullable List<String> obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public List<String> deserializeStringList(@NonNull String val) {
        return gson.fromJson(val, new TypeToken<List<String>>() {
        }.getType());
    }
    //endregion

    // region MediaType
    @TypeConverter
    @Nullable
    public String serializeMediaType(@Nullable MediaType obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public MediaType deserializeMediaType(@NonNull String val) {
        return gson.fromJson(val, MediaType.class);
    }
    //endregion

    // region NSFWRating
    @TypeConverter
    @Nullable
    public String serializeNSFWRating(@Nullable NSFWRating obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public NSFWRating deserializeNSFWRating(@NonNull String val) {
        return gson.fromJson(val, NSFWRating.class);
    }
    //endregion

    // region BroadcastStatus
    @TypeConverter
    @Nullable
    public String serializeBroadcastStatus(@Nullable BroadcastStatus obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public BroadcastStatus deserializeBroadcastStatus(@NonNull String val) {
        return gson.fromJson(val, BroadcastStatus.class);
    }
    //endregion

    // region Source
    @TypeConverter
    @Nullable
    public String serializeSource(@Nullable Source obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public Source deserializeSource(@NonNull String val) {
        return gson.fromJson(val, Source.class);
    }
    //endregion

    // region ContentRating
    @TypeConverter
    @Nullable
    public String serializeContentRating(@Nullable ContentRating obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public ContentRating deserializeContentRating(@NonNull String val) {
        return gson.fromJson(val, ContentRating.class);
    }
    //endregion

    // region DayOfWeek
    @TypeConverter
    @Nullable
    public String serializeDayOfWeek(@Nullable DayOfWeek obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public DayOfWeek deserializeDayOfWeek(@NonNull String val) {
        return gson.fromJson(val, DayOfWeek.class);
    }
    //endregion

    // region YearSeason
    @TypeConverter
    @Nullable
    public String serializeYearSeason(@Nullable YearSeason obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public YearSeason deserializeYearSeason(@NonNull String val) {
        return gson.fromJson(val, YearSeason.class);
    }
    //endregion

    // region LibraryEntryStatus
    @TypeConverter
    @Nullable
    public String serializeLibraryEntryStatus(@Nullable LibraryEntryStatus obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public LibraryEntryStatus deserializeLibraryEntryStatus(@NonNull String val) {
        return gson.fromJson(val, LibraryEntryStatus.class);
    }
    //endregion

    // region RelationType
    @TypeConverter
    @Nullable
    public String serializeRelationType(@Nullable RelationType obj) {
        if (isNull(obj))
            return null;

        return gson.toJson(obj);
    }

    @TypeConverter
    @NonNull
    public RelationType deserializeRelationType(@NonNull String val) {
        return gson.fromJson(val, RelationType.class);
    }
    //endregion
}
