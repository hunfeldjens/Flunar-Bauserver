package eu.hunfeld.flunarbauserver.model;

import java.util.List;


public record ModerationHistoryPage(
    int page, int pages, long totalEntries, List<ModerationRecord> entries) {
  public ModerationHistoryPage {
    entries = List.copyOf(entries);
  }
}
