package io.github.shadow578.tenshi.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import io.github.shadow578.tenshi.mal.model.Season;
import io.github.shadow578.tenshi.mal.model.type.DayOfWeek;
import io.github.shadow578.tenshi.mal.model.type.YearSeason;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class DateHelper {

    // region get date and time

    /**
     * UTC timezone
     */
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    /**
     * default timezone of the device
     */
    public static final ZoneId DEVICE_ZONE = ZoneId.systemDefault();

    /**
     * timezone of Asia/Tokyo
     */
    public static final ZoneId JAPAN_ZONE = ZoneId.of("Asia/Tokyo");

    /**
     * the date and time in the device's time zone
     */
    private static ZonedDateTime local() {
        return ZonedDateTime.now();
    }

    /**
     * the date and time in Tokyo
     */
    private static ZonedDateTime jp() {
        return local().withZoneSameInstant(JAPAN_ZONE);
    }

    /**
     * get the epoch value of a datetime in the UTC time zone
     *
     * @param time the time to convert. may be in any timezone
     * @return the epoch value (seconds)
     */
    public static long toEpoch(@NonNull ZonedDateTime time) {
        return time.withZoneSameInstant(UTC_ZONE).toEpochSecond();
    }

    /**
     * convert a epoch value to a date and time in the devices timezone
     *
     * @param epoch the epoch value (seconds, in UTC) as returned by {@link #toEpoch(ZonedDateTime)}
     * @return the datetime in the local zone
     */
    @NonNull
    public static ZonedDateTime fromEpoc(long epoch) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), UTC_ZONE).withZoneSameInstant(DEVICE_ZONE);
    }

    /**
     * get the number of years between the date and now
     *
     * @param date the date to check
     * @return the number of years between now and the date
     */
    public static int getYearsToNow(LocalDate date) {
        return Period.between(date, getLocalTime().toLocalDate()).getYears();
    }

    /**
     * get the date and time in the device's time zone
     *
     * @return date and time in the local timezone
     */
    public static ZonedDateTime getLocalTime() {
        return local();
    }

    /**
     * get the date and time in Tokyo
     *
     * @return date and time in Tokyo
     */
    public static ZonedDateTime getJapanTime() {
        return jp();
    }

    /**
     * get the current season
     *
     * @return the current season
     */
    public static Season getSeason() {
        return new Season(getYear(), getYearSeason());
    }

    /**
     * get the current year
     *
     * @return the year, eg. 2020
     */
    public static int getYear() {
        return local().getYear();
    }

    /**
     * get the current season of {@link YearSeason}
     *
     * @return the current season
     */
    public static YearSeason getYearSeason() {
        switch (local().getMonth()) {
            case JANUARY:
            case FEBRUARY:
            case MARCH:
                return YearSeason.Winter;
            case APRIL:
            case MAY:
            case JUNE:
                return YearSeason.Spring;
            case JULY:
            case AUGUST:
            case SEPTEMBER:
                return YearSeason.Summer;
            case OCTOBER:
            case NOVEMBER:
            case DECEMBER:
            default:
                return YearSeason.Fall;
        }
    }

    /**
     * convert from {@link java.time.DayOfWeek} to {@link DayOfWeek}
     *
     * @param javaDoW the java8 day of week
     * @return the MAL model day of week
     */
    @NonNull
    public static DayOfWeek convertDayOfWeek(@NonNull java.time.DayOfWeek javaDoW) {
        switch (javaDoW) {
            default:
                // any other day just defaults to monday
                // really doesn't matter what this would default to
            case MONDAY:
                return DayOfWeek.Monday;
            case TUESDAY:
                return DayOfWeek.Tuesday;
            case WEDNESDAY:
                return DayOfWeek.Wednesday;
            case THURSDAY:
                return DayOfWeek.Thursday;
            case FRIDAY:
                return DayOfWeek.Friday;
            case SATURDAY:
                return DayOfWeek.Saturday;
            case SUNDAY:
                return DayOfWeek.Sunday;
        }
    }
    // endregion

    // region formatting
    /**
     * formatting for LocalDate
     */
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    /**
     * formatting for LocalTime
     */
    private static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

    /**
     * formatting for ZonedDateTime
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    /**
     * format to a string in the device's local date formatting
     *
     * @param date the date to format
     * @param def  default value if formatting fails
     * @return the formatted date string
     */
    @Nullable
    public static String format(@Nullable LocalDate date, @Nullable String def) {
        if (isNull(date))
            return def;
        return date.format(LOCAL_DATE_FORMATTER);
    }

    /**
     * format to a string in the device's local time formatting
     *
     * @param time the time to format
     * @param def  default value if formatting fails
     * @return the formatted date string
     */
    @Nullable
    public static String format(@Nullable LocalTime time, @Nullable String def) {
        if (isNull(time))
            return def;
        return time.format(LOCAL_TIME_FORMATTER);
    }

    /**
     * format to a string in the device's local date / time formatting and the device's timezone
     *
     * @param dateTime the date and time to format
     * @param def      default value if formatting fails
     * @return the formatted date string
     */
    @Nullable
    public static String format(@Nullable ZonedDateTime dateTime, @Nullable String def) {
        if (isNull(dateTime))
            return def;
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    //endregion
}
