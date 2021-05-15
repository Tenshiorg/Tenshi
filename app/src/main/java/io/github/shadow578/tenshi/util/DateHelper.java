package io.github.shadow578.tenshi.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import io.github.shadow578.tenshi.mal.model.Season;
import io.github.shadow578.tenshi.mal.model.type.YearSeason;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class DateHelper {

    // region get date and time

    /**
     * the date and time in the device's time zone
     */
    private static LocalDateTime local() {
        return LocalDateTime.now();
    }

    /**
     * the date and time in Tokyo
     */
    private static LocalDateTime jp() {
        return LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
    }

    /**
     * get the epoch value of a datetime
     *
     * @return the epoch value (seconds)
     */
    public static long toEpoch(@NonNull LocalDateTime time) {
        return time.toEpochSecond(ZoneOffset.UTC);
    }

    /**
     * convert a epoch value to the device's local date and time
     *
     * @param epoch the epoch value (seconds)
     * @return the local datetime
     */
    @NonNull
    public static LocalDateTime fromEpoc(long epoch) {
        return LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC);
    }

    /**
     * get the number of years between the date and now
     * @param date the date to check
     * @return the number of years between now and the date
     */
    public static int getYearsToNow(LocalDate date){
        return Period.between(date, getLocalTime().toLocalDate()).getYears();
    }

    /**
     * get the date and time in the device's time zone
     *
     * @return date and time in the local timezone
     */
    public static LocalDateTime getLocalTime() {
        return local();
    }

    /**
     * get the date and time in Tokyo
     *
     * @return date and time in Tokyo
     */
    public static LocalDateTime getJapanTime() {
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
