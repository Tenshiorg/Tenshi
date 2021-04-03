package io.github.shadow578.tenshi.mal.model;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

import java.time.LocalTime;

import io.github.shadow578.tenshi.mal.model.type.DayOfWeek;

/**
 * information about the broadcast schedule of a {@link Anime}
 */
public final class BroadcastInfo {
    /**
     * the day that the anime is released in a week
     */
    @SerializedName("day_of_the_week")
    @ColumnInfo(name = "day_of_the_week")
    public DayOfWeek weekday;

    /**
     * time of the broadcast, HH:MM
     */
    @SerializedName("start_time")
    @ColumnInfo(name = "start_time")
    public LocalTime startTime;
}
