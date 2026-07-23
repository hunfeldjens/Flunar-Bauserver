package eu.hunfeld.flunarbauserver.service;

import eu.hunfeld.flunarbauserver.FlunarBauserver;
import eu.hunfeld.flunarbauserver.settings.LabyModSettings;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;


public final class ServerListIconService implements Listener {
  private final FlunarBauserver plugin;
  private volatile CachedServerIcon icon;

  public ServerListIconService(FlunarBauserver plugin, LabyModSettings.ServerListIcon settings) {
    this.plugin = plugin;
    if (settings.enabled() && !settings.imageUrl().isBlank()) load(settings.imageUrl());
  }

  @EventHandler
  public void ping(ServerListPingEvent event) {
    CachedServerIcon current = icon;
    if (current != null) event.setServerIcon(current);
  }

  private void load(String configuredUrl) {
    URI uri;
    try {
      uri = URI.create(configuredUrl);
      if (!uri.getScheme().equalsIgnoreCase("https") && !uri.getScheme().equalsIgnoreCase("http"))
        throw new IllegalArgumentException("Nur HTTP/HTTPS ist erlaubt");
    } catch (RuntimeException exception) {
      plugin.getLogger().warning("Ungültige Serverlisten-Icon-URL: " + exception.getMessage());
      return;
    }
    HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).GET().build();
    HttpClient client =
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    client
        .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
        .thenApply(
            response -> {
              if (response.statusCode() / 100 != 2 || response.body().length > 2_000_000)
                throw new IllegalStateException("HTTP " + response.statusCode());
              return response.body();
            })
        .thenApply(ServerListIconService::decode)
        .whenComplete(
            (image, error) ->
                Bukkit.getScheduler()
                    .runTask(
                        plugin,
                        () -> {
                          if (error != null || image == null) {
                            plugin
                                .getLogger()
                                .warning(
                                    "Serverlisten-Icon konnte nicht geladen werden: "
                                        + (error == null
                                            ? "unbekanntes Bildformat"
                                            : error.getMessage()));
                            return;
                          }
                          try {
                            icon = Bukkit.loadServerIcon(image);
                          } catch (Exception exception) {
                            plugin
                                .getLogger()
                                .warning(
                                    "Serverlisten-Icon ist ungültig: " + exception.getMessage());
                          }
                        }))
        .whenComplete((_, _) -> client.close());
  }

  private static BufferedImage decode(byte[] bytes) {
    try {
      BufferedImage source = ImageIO.read(new ByteArrayInputStream(bytes));
      if (source == null) return null;
      if (source.getWidth() == 64 && source.getHeight() == 64) return source;
      BufferedImage scaled = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = scaled.createGraphics();
      graphics.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics.drawImage(source, 0, 0, 64, 64, null);
      graphics.dispose();
      return scaled;
    } catch (Exception exception) {
      throw new IllegalStateException("Bild konnte nicht gelesen werden", exception);
    }
  }
}
