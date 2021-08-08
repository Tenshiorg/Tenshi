package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import io.github.shadow578.tenshi.mal.model.type.DayOfWeek;
import io.github.shadow578.tenshi.util.DateHelper;

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


    /**
     * get the next time this broadcast is scheduled.
     * this does not check for the end of a anime, only for the broadcast time and weekday
     *
     * @param start the start date and time to check from
     * @return the next scheduled broadcast
     */
    @NonNull
    public ZonedDateTime getNextBroadcast(@NonNull ZonedDateTime start) {
        // change the time to be correct
        ZonedDateTime nextBroadcast = start.with(startTime);

        // increment current date until we find a date with the right weekday
        while (!DateHelper.convertDayOfWeek(nextBroadcast.getDayOfWeek()).equals(weekday))
            nextBroadcast = nextBroadcast.plusDays(1);

        // this should be the next scheduled broadcast
        return nextBroadcast;
    }
}