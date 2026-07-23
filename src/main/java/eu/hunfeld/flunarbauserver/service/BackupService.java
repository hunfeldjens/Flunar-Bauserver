package eu.hunfeld.flunarbauserver.service;

import eu.hunfeld.flunarbauserver.FlunarBauserver;
import eu.hunfeld.flunarbauserver.database.Sql;
import eu.hunfeld.flunarbauserver.settings.Settings;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public final class BackupService implements AutoCloseable {
  private final FlunarBauserver plugin;
  private final Settings settings;
  private final ExecutorService executor =
      Executors.newSingleThreadExecutor(Thread.ofPlatform().name("flunar-backup").factory());
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean safeWorldLock = new AtomicBoolean(false);
  private volatile boolean reservedSafe;
  private volatile Process process;

  public BackupService(FlunarBauserver plugin, Settings settings) {
    this.plugin = plugin;
    this.settings = settings;
  }

  public boolean running() {
    return running.get();
  }

  public boolean safeWorldLocked(String worldName) {
    return safeWorldLock.get()
        && !Sql.cleanWorld(worldName).equals(Sql.cleanWorld(settings.mainWorld()));
  }

  public CompletableFuture<Integer> start(boolean safe) {
    if (!tryReserve(safe)) return CompletableFuture.completedFuture(Integer.MIN_VALUE);
    return executeReserved();
  }

  public boolean tryReserve(boolean safe) {
    if (!running.compareAndSet(false, true)) return false;
    reservedSafe = safe;
    safeWorldLock.set(safe);
    return true;
  }

  public CompletableFuture<Integer> executeReserved() {
    if (!running.get()) return CompletableFuture.completedFuture(Integer.MIN_VALUE);
    return CompletableFuture.supplyAsync(
            () -> runScript(settings.paths().backupScript(), reservedSafe), executor)
        .whenComplete(
            (_, _) -> {
              safeWorldLock.set(false);
              running.set(false);
            });
  }

  private int runScript(Path script, boolean safe) {
    if (!Files.isRegularFile(script) || !Files.isExecutable(script)) {
      plugin.getLogger().severe("Backup-Skript fehlt oder ist nicht ausführbar: " + script);
      return 126;
    }
    ProcessBuilder builder = new ProcessBuilder(script.toString(), safe ? "safe" : "unsafe");
    builder.directory(settings.paths().serverRoot().toFile());
    builder.redirectErrorStream(true);
    try {
      process = builder.start();
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) plugin.getLogger().info("[Backup] " + line);
      }
      return process.waitFor();
    } catch (Exception exception) {
      if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
      plugin.getLogger().log(Level.SEVERE, "Backup-Prozess fehlgeschlagen", exception);
      return 1;
    }
  }

  public void cancel() {
    Process active = process;
    if (active != null && active.isAlive()) active.destroyForcibly();
    process = null;
    safeWorldLock.set(false);
    running.set(false);
  }

  @Override
  public void close() {
    executor.shutdownNow();
    cancel();
    safeWorldLock.set(false);
    running.set(false);
  }
}
