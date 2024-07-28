package com.example.common.utils;

import com.example.common.enums.DateTimeFormat;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateTimeUtils {

    public static String getCurrentDateTime(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    @SneakyThrows
    public static String covertDateTime(String dateTime, String format) {
        return new SimpleDateFormat(format).format(DateUtils.parseDate(dateTime, DateTimeFormat.getFormats()));
    }

    public static long getDifferenceInDays(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.DAYS.between(startDateTime, endDateTime);
    }

    public static long getDifferenceInHours(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    public static long getDifferenceInMinutes(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    public static long getDifferenceInSeconds(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.SECONDS.between(startDateTime, endDateTime);
    }

    public static long getDifferenceInMinutes(LocalTime startTime, LocalTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        return duration.toMinutes();
    }

    public static long getDifferenceInSeconds(LocalTime startTime, LocalTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        return duration.getSeconds();
    }
}
