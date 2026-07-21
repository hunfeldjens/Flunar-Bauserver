package eu.hunfeld.flunarbauserver.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeFormats {
  private static final int MINUTE = 60;
  private static final int HOUR = 3_600;
  private static final int DAY = 86_400;
  private static final int MONTH = 2_592_000;
  private static final DateTimeFormatter DATE_TIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private TimeFormats() {}

  public static String daysHoursMinutes(int seconds) {
    int total = Math.max(0, seconds);
    int days = total / DAY;
    int hours = total % DAY / HOUR;
    int minutes = total % HOUR / MINUTE;
    return days + "d " + hours + "h " + twoDigits(minutes) + "m";
  }

  public static String fullDuration(int seconds) {
    int total = Math.max(0, seconds);
    int months = total / MONTH;
    int rest = total % MONTH;
    int days = rest / DAY;
    int hours = rest % DAY / HOUR;
    int minutes = rest % HOUR / MINUTE;
    if (months > 0)
      return months + " Monat(e) " + days + "d " + hours + "h " + twoDigits(minutes) + "m";
    if (days > 0) return days + "d " + hours + "h " + twoDigits(minutes) + "m";
    return hours + "h " + twoDigits(minutes) + "m";
  }

  public static String teamDuration(int seconds) {
    int total = Math.max(0, seconds);
    return total / DAY
        + " Tage, "
        + total % DAY / HOUR
        + " Stunden, "
        + total % HOUR / MINUTE
        + " Minuten";
  }

  public static String dateTime(LocalDateTime value) {
    return value == null ? "-" : DATE_TIME.format(value);
  }

  private static String twoDigits(int value) {
    return value < 10 ? "0" + value : Integer.toString(value);
  }
}
