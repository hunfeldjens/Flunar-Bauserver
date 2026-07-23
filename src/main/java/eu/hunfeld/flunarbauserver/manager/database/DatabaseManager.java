package eu.hunfeld.flunarbauserver.manager.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.hunfeld.flunarbauserver.FlunarBauserver;
import eu.hunfeld.flunarbauserver.database.CacheRepository;
import eu.hunfeld.flunarbauserver.database.SchemaInitializer;
import eu.hunfeld.flunarbauserver.settings.DatabaseSettings;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.mariadb.jdbc.Driver;


@SuppressWarnings("SpellCheckingInspection")
public final class DatabaseManager implements AutoCloseable {
  private final FlunarBauserver plugin;
  private final DatabaseSettings settings;
  private final ExecutorService executor;
  private final List<CacheRepository> repositories = new ArrayList<>();
  private final AtomicBoolean ready = new AtomicBoolean(false);
  private final AtomicBoolean closing = new AtomicBoolean(false);
  private volatile HikariDataSource dataSource;

  public DatabaseManager(FlunarBauserver plugin, DatabaseSettings settings) {
    this.plugin = plugin;
    this.settings = settings;
    this.executor =
        Executors.newFixedThreadPool(
            settings.poolSize(), Thread.ofPlatform().name("flunar-database-", 0).factory());
  }

  public void register(CacheRepository repository) {
    if (ready.get()) throw new IllegalStateException("Repository wurde zu spät registriert");
    repositories.add(repository);
  }

  public CompletableFuture<Boolean> initialise() {
    if (!settings.configured()) {
      plugin
          .getLogger()
          .severe("database.yml ist nicht konfiguriert. DB-Funktionen bleiben sicher gesperrt.");
      return CompletableFuture.completedFuture(false);
    }
    long started = System.nanoTime();
    plugin.getLogger().info("Datenbankinitialisierung gestartet: " + settings.safeTarget());
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            dataSource = new HikariDataSource(createConfig());
            try (Connection connection = connection()) {
              plugin.getLogger().info("MariaDB-Verbindung hergestellt; Schema wird geprüft.");
              SchemaInitializer.create(connection);
              plugin.getLogger().info("Datenbankschema ist bereit; Caches werden geladen.");
              for (CacheRepository repository : repositories) repository.load(connection);
            }
            ready.set(true);
            plugin
                .getLogger()
                .info(
                    "MariaDB vollständig bereit; "
                        + repositories.size()
                        + " Datenmodule in "
                        + elapsedMillis(started)
                        + " ms geladen.");
            return true;
          } catch (Exception exception) {
            ready.set(false);
            plugin
                .getLogger()
                .log(
                    Level.SEVERE,
                    "Datenbankstart nach "
                        + elapsedMillis(started)
                        + " ms fehlgeschlagen ("
                        + settings.safeTarget()
                        + ").",
                    exception);
            closePool();
            return false;
          }
        },
        executor);
  }

  public CompletableFuture<Boolean> reloadCaches() {
    return submit(
        connection -> {
          for (CacheRepository repository : repositories) repository.load(connection);
          return true;
        },
        false);
  }

  public <T> CompletableFuture<T> submit(SqlFunction<Connection, T> operation, T failureValue) {
    if (!ready.get() || closing.get()) return CompletableFuture.completedFuture(failureValue);
    return CompletableFuture.supplyAsync(
        () -> {
          try (Connection connection = connection()) {
            return operation.apply(connection);
          } catch (Exception exception) {
            plugin
                .getLogger()
                .log(Level.SEVERE, "Asynchrone Datenbankaktion fehlgeschlagen", exception);
            return failureValue;
          }
        },
        executor);
  }

  public boolean isReady() {
    return ready.get();
  }

  private long elapsedMillis(long started) {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
  }

  private HikariConfig createConfig() {
    HikariConfig config = new HikariConfig();
    config.setPoolName("Flunar-Bauserver");
    config.setDriverClassName(Driver.class.getName());
    config.setJdbcUrl(settings.jdbcUrl());
    config.setUsername(settings.username());
    config.setPassword(settings.password());
    config.setMaximumPoolSize(settings.poolSize());
    config.setMinimumIdle(1);
    config.setConnectionTimeout(settings.connectionTimeoutMs());
    config.setValidationTimeout(2_000L);
    config.setInitializationFailTimeout(settings.connectionTimeoutMs());
    config.addDataSourceProperty(
        "connectTimeout", Math.toIntExact(settings.connectionTimeoutMs()));
    config.addDataSourceProperty("socketTimeout", 30_000);
    config.addDataSourceProperty("rewriteBatchedStatements", true);
    return config;
  }

  private Connection connection() throws SQLException {
    HikariDataSource source = dataSource;
    if (source == null || source.isClosed())
      throw new SQLException("Datenbank-Pool ist nicht verfügbar");
    return source.getConnection();
  }

  @Override
  public void close() {
    if (!closing.compareAndSet(false, true)) return;
    ready.set(false);
    executor.shutdown();
    try {
      if (!executor.awaitTermination(8, TimeUnit.SECONDS)) executor.shutdownNow();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      executor.shutdownNow();
    }
    closePool();
    repositories.forEach(CacheRepository::clear);
  }

  private void closePool() {
    HikariDataSource source = dataSource;
    dataSource = null;
    if (source != null) source.close();
  }

  @FunctionalInterface
  public interface SqlFunction<T, R> {
    R apply(T value) throws Exception;
  }
}
