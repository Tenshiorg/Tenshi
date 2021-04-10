package io.github.shadow578.tenshi.util.converter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.*;

/**
 * handles zoned date-time values from MAL.
 * Format "2015-03-02T06:03:11+00:00" / ISO 8601
 */
public class GSONZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public void write(JsonWriter out, ZonedDateTime value) throws IOException {
        if(isNull(value))
            out.nullValue();
        else
            out.value(FORMAT.format(value));
    }

    @Override
    public ZonedDateTime read(JsonReader in) throws IOException {
        if(in.peek().equals(JsonToken.NULL))
        {
            in.nextNull();
            return null;
        }else
            return ZonedDateTime.parse(in.nextString(), FORMAT);
    }
}