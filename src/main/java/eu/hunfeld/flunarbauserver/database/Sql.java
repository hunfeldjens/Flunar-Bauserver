package eu.hunfeld.flunarbauserver.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

public final class Sql {
  private Sql() {}

  public static String cleanWorld(String value) {
    if (value == null) return "";
    String clean = value.toLowerCase(Locale.ROOT).trim();
    if (clean.startsWith("projekt_")) clean = clean.substring(8);
    else if (clean.startsWith("privat_")) clean = clean.substring(7);
    int separator = clean.indexOf(':');
    if (separator >= 0 && separator + 1 < clean.length()) clean = clean.substring(separator + 1);
    return clean;
  }

  public static UUID uuid(String value) {
    try {
      return value == null || value.isBlank() ? null : UUID.fromString(value);
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }

  public static String text(ResultSet result, String column) throws SQLException {
    String value = result.getString(column);
    return value == null ? "" : value;
  }

  public static Double nullableDouble(ResultSet result, String column) throws SQLException {
    double value = result.getDouble(column);
    return result.wasNull() ? null : value;
  }

  public static Float nullableFloat(ResultSet result, String column) throws SQLException {
    float value = result.getFloat(column);
    return result.wasNull() ? null : value;
  }

  public static LocalDateTime localDateTime(Timestamp value) {
    return value == null ? null : value.toLocalDateTime();
  }

  public static void nullableNumber(PreparedStatement statement, int index, Number value)
      throws SQLException {
    if (value == null) statement.setNull(index, Types.DOUBLE);
    else statement.setDouble(index, value.doubleValue());
  }
}
