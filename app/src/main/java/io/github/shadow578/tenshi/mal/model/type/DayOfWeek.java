package io.github.shadow578.tenshi.mal.model.type;

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
    Sunday
}
