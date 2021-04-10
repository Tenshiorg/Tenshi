package io.github.shadow578.tenshi.mal.model;

import androidx.room.ColumnInfo;

import io.github.shadow578.tenshi.mal.model.type.YearSeason;

/**
 * a season with year (eg. Summer 2020)
 */
public final class Season {
    /**
     * the season of the year
     */
    @ColumnInfo(name = "season")
    public YearSeason season;

    /**
     * four digit year
     */
    @ColumnInfo(name = "year")
    public int year;

    public Season(int year, YearSeason season) {
        this.year = year;
        this.season = season;
    }

    public boolean equals(Season o) {
        return year == o.year && season.equals(o.season);
    }
}
