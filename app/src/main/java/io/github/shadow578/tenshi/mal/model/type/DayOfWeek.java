package io.github.shadow578.tenshi.mal.model.type;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * the day of a week (for example, on which a {@link io.github.shadow578.tenshi.mal.model.Anime} broadcasts)
 */
public enum DayOfWeek {
    /**
     * monday
     */
    @SerializedName("monday")
    Monday,

    /**
     * tuesday
     */
    @SerializedName("tuesday")
    Tuesday,

    /**
     * wednesday
     */
    @SerializedName("wednesday")
    Wednesday,

    /**
     * thursday
     */
    @SerializedName("thursday")
    Thursday,

    /**
     * friday
     */
    @SerializedName("friday")
    Friday,

    /**
     * saturday
     */
    @SerializedName("saturday")
    Saturday,

    /**
     * sunday
     */
    @SerializedName("sunday")
    Sunday;

    /**
     * @return the next weekday
     */
    @NonNull
    public DayOfWeek next() {
        switch (this) {
            default:
                // this won't ever happen, so it doesn't matter
            case Monday:
                return Tuesday;
            case Tuesday:
                return Wednesday;
            case Wednesday:
                return Thursday;
            case Thursday:
                return Friday;
            case Friday:
                return Saturday;
            case Saturday:
                return Sunday;
            case Sunday:
                return Monday;
        }
    }

    /**
     * @return the previous weekday
     */
    @NonNull
    public DayOfWeek previous() {
        switch (this) {
            default:
                // this won't ever happen, so it doesn't matter
            case Monday:
                return Sunday;
            case Tuesday:
                return Monday;
            case Wednesday:
                return Tuesday;
            case Thursday:
                return Wednesday;
            case Friday:
                return Thursday;
            case Saturday:
                return Friday;
            case Sunday:
                return Saturday;
        }
    }


}
