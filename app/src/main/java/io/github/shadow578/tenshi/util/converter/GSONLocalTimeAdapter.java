package io.github.shadow578.tenshi.util.converter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static io.github.shadow578.tenshi.lang.LanguageUtils.*;

/**
 * handles time values from MAL
 * Format "01:35"
 */
public class GSONLocalTimeAdapter extends TypeAdapter<LocalTime> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_TIME;

    @Override
    public void write(JsonWriter out, LocalTime value) throws IOException {
        if(isNull(value))
            out.nullValue();
        else
            out.value(FORMAT.format(value));
    }

    @Override
    public LocalTime read(JsonReader in) throws IOException {
        if(in.peek().equals(JsonToken.NULL))
        {
            in.nextNull();
            return null;
        }else
            return LocalTime.parse(in.nextString(), FORMAT);
    }
}
