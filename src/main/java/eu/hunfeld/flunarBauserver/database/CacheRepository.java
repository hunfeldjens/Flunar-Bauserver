package eu.hunfeld.flunarBauserver.database;

import java.sql.Connection;

public interface CacheRepository {
  void load(Connection connection) throws Exception;

  void clear();
}
