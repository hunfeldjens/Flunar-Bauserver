package eu.hunfeld.flunarBauserver.model;

import java.util.List;

/** Eine bereits begrenzte Datenbankseite der Moderationshistorie. */
public record ModerationHistoryPage(
    int page, int pages, long totalEntries, List<ModerationRecord> entries) {
  public ModerationHistoryPage {
    entries = List.copyOf(entries);
  }
}
