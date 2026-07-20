package eu.hunfeld.flunarbauserver.database;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

public final class SchemaInitializer {
  private SchemaInitializer() {}

  public static void create(Connection connection) throws Exception {
    List<String> statements =
        List.of(
            "CREATE TABLE IF NOT EXISTS server_kicks (id INT AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(36), name VARCHAR(36), by_uuid VARCHAR(36), by_name VARCHAR(36), reason VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS server_bans (id INT AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(36), name VARCHAR(36), by_uuid VARCHAR(36), by_name VARCHAR(36), reason VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, active TINYINT(1) DEFAULT 1, INDEX idx_uuid_active (uuid, active))",
            "CREATE TABLE IF NOT EXISTS bau_projekte (projekt_name VARCHAR(64) PRIMARY KEY, beschreibung TEXT, weltname VARCHAR(64), owner_uuid VARCHAR(36) DEFAULT NULL, whitelist_active TINYINT(1) DEFAULT 1, icon VARCHAR(64) DEFAULT 'map')",
            "ALTER TABLE bau_projekte ADD COLUMN IF NOT EXISTS owner_uuid VARCHAR(36) DEFAULT NULL",
            "ALTER TABLE bau_projekte ADD COLUMN IF NOT EXISTS whitelist_active TINYINT(1) DEFAULT 1",
            "ALTER TABLE bau_projekte ADD COLUMN IF NOT EXISTS icon VARCHAR(64) DEFAULT 'map'",
            "CREATE TABLE IF NOT EXISTS projekt_bans (world VARCHAR(64), uuid VARCHAR(36), PRIMARY KEY(world, uuid))",
            "CREATE TABLE IF NOT EXISTS projekt_whitelist (world VARCHAR(64), uuid VARCHAR(36), PRIMARY KEY(world, uuid))",
            "CREATE TABLE IF NOT EXISTS projekt_autoload (world VARCHAR(64) PRIMARY KEY)",
            "CREATE TABLE IF NOT EXISTS projekt_infos (id INT AUTO_INCREMENT PRIMARY KEY, world VARCHAR(64), name VARCHAR(64), beschreibung TEXT, created_by VARCHAR(36), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, x DOUBLE DEFAULT NULL, y DOUBLE DEFAULT NULL, z DOUBLE DEFAULT NULL, yaw FLOAT DEFAULT NULL, pitch FLOAT DEFAULT NULL, INDEX idx_world (world))",
            "ALTER TABLE projekt_infos ADD COLUMN IF NOT EXISTS x DOUBLE DEFAULT NULL",
            "ALTER TABLE projekt_infos ADD COLUMN IF NOT EXISTS y DOUBLE DEFAULT NULL",
            "ALTER TABLE projekt_infos ADD COLUMN IF NOT EXISTS z DOUBLE DEFAULT NULL",
            "ALTER TABLE projekt_infos ADD COLUMN IF NOT EXISTS yaw FLOAT DEFAULT NULL",
            "ALTER TABLE projekt_infos ADD COLUMN IF NOT EXISTS pitch FLOAT DEFAULT NULL",
            "CREATE TABLE IF NOT EXISTS privat_worlds (uuid VARCHAR(36) PRIMARY KEY, weltname VARCHAR(64))",
            "CREATE TABLE IF NOT EXISTS server_features (feature VARCHAR(32) PRIMARY KEY, aktiv TINYINT(1) DEFAULT 1)",
            "CREATE TABLE IF NOT EXISTS player_data (uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(36), ip_adresse VARCHAR(45), first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP, last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "ALTER TABLE player_data ADD COLUMN IF NOT EXISTS first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE player_data ADD COLUMN IF NOT EXISTS last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            "CREATE TABLE IF NOT EXISTS onlinetime (uuid VARCHAR(36) PRIMARY KEY, onlinetime_aktiv INT DEFAULT 0, onlinetime_afk INT DEFAULT 0, joins INT DEFAULT 0)");
    try (Statement statement = connection.createStatement()) {
      statement.setQueryTimeout(30);
      for (String sql : statements) statement.executeUpdate(sql);
    }
  }
}
