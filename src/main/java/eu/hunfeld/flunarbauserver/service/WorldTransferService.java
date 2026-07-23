package eu.hunfeld.flunarbauserver.service;

import eu.hunfeld.flunarbauserver.FlunarBauserver;
import eu.hunfeld.flunarbauserver.settings.Settings;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public final class WorldTransferService implements AutoCloseable {
  private final FlunarBauserver plugin;
  private final Settings settings;
  private final ExecutorService executor =
      Executors.newSingleThreadExecutor(
          Thread.ofPlatform().name("flunar-world-transfer").factory());

  public WorldTransferService(FlunarBauserver plugin, Settings settings) {
    this.plugin = plugin;
    this.settings = settings;
  }

  public CompletableFuture<Integer> export(String namespace, String worldName, String exportName) {
    String relative = namespace + "/" + worldName;
    Path source =
        settings.paths().serverRoot().resolve("world/dimensions").resolve(relative).normalize();
    Path dimensions = settings.paths().serverRoot().resolve("world/dimensions").normalize();
    if (!source.startsWith(dimensions) || !Files.isDirectory(source))
      return CompletableFuture.completedFuture(2);
    Path script = settings.paths().exportScript();
    if (!Files.isRegularFile(script) || !Files.isExecutable(script))
      return CompletableFuture.completedFuture(126);
    return CompletableFuture.supplyAsync(() -> run(script, relative, exportName), executor);
  }

  public CompletableFuture<Integer> pushTemplate(String worldName) {
    String configured = settings.templates().get(worldName.toLowerCase());
    if (configured == null) return CompletableFuture.completedFuture(2);
    Path target = Path.of(configured).toAbsolutePath().normalize();
    Path source =
        settings
            .paths()
            .serverRoot()
            .resolve("world/dimensions/projekt")
            .resolve(worldName)
            .normalize();
    if (!Files.isDirectory(source)
        || target.getNameCount() < 3
        || target.equals(target.getRoot())
        || target.equals(settings.paths().serverRoot().toAbsolutePath().normalize()))
      return CompletableFuture.completedFuture(2);
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Files.createDirectories(target);
            try (var paths = Files.walk(target)) {
              for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
                if (!path.equals(target)) Files.deleteIfExists(path);
            }
            try (var paths = Files.walk(source)) {
              for (Path path : paths.toList()) {
                if (Files.isSymbolicLink(path)) continue;
                Path destination = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) Files.createDirectories(destination);
                else
                  Files.copy(
                      path,
                      destination,
                      StandardCopyOption.REPLACE_EXISTING,
                      StandardCopyOption.COPY_ATTRIBUTES);
              }
            }
            return 0;
          } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Template-Push fehlgeschlagen", exception);
            return 1;
          }
        },
        executor);
  }

  @SuppressWarnings("resource")
  private int run(Path script, String worldPath, String exportName) {
    ProcessBuilder builder =
        new ProcessBuilder("/bin/bash", script.toString(), worldPath, exportName);
    builder.directory(settings.paths().serverRoot().toFile());
    builder.redirectErrorStream(true);
    try {
      Process process = builder.start();
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) plugin.getLogger().info("[Welt-Export] " + line);
      }
      return process.waitFor();
    } catch (Exception exception) {
      if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
      plugin.getLogger().log(Level.SEVERE, "Welt-Export fehlgeschlagen", exception);
      return 1;
    }
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }
}
