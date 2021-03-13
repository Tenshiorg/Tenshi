package io.github.shadow578.tenshi.util.converter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import static io.github.shadow578.tenshi.lang.LanguageUtils.*;

/**
 * handles local date values from MAL.
 * Format "2017-10-23" or "2017-10" or "2017"
 */
public class GSONLocalDateAdapter extends TypeAdapter<LocalDate> {

    private static final DateTimeFormatter FORMAT_W = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter FORMAT_R = new DateTimeFormatterBuilder()
            .appendPattern("[yyyy-MM-dd][yyyy-MM][yyyy]")
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .toFormatter();

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (isNull(value))
            out.nullValue();
        else
            out.value(FORMAT_W.format(value));
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        if (in.peek().equals(JsonToken.NULL)) {
            in.nextNull();
            return null;
        } else
            return LocalDate.parse(in.nextString(), FORMAT_R);
    }
}
