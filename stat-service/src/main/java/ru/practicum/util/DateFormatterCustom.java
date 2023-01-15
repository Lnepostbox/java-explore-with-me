package ru.practicum.util;

import org.springframework.stereotype.Component;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateFormatterCustom {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String dateToString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(formatter);
    }

    public LocalDateTime stringToDate(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        String decodeDate = URLDecoder.decode(dateTime, StandardCharsets.UTF_8);
        return LocalDateTime.parse(decodeDate, formatter);
    }
}
