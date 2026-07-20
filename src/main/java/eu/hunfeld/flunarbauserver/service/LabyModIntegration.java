package eu.hunfeld.flunarbauserver.service;

import eu.hunfeld.flunarbauserver.FlunarBauserver;
import eu.hunfeld.flunarbauserver.settings.LabyModSettings;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * Classloader-safe entry point for the optional LabyMod API.
 *
 * <p>This class deliberately has no LabyMod type in its signature or constant pool. Paper can
 * therefore load the Bauserver plugin even when LabyModServerAPI is not installed.
 */
public final class LabyModIntegration {
  private static final String IMPLEMENTATION = "eu.hunfeld.flunarbauserver.service.LabyModService";

  private LabyModIntegration() {}

  public static void registerIfAvailable(FlunarBauserver plugin, LabyModSettings settings) {
    if (!settings.enabled()) {
      plugin.getLogger().info("LabyMod-Integration ist in labymod.yml deaktiviert.");
      return;
    }
    if (!Bukkit.getPluginManager().isPluginEnabled("LabyModServerAPI")) {
      plugin
          .getLogger()
          .info("LabyModServerAPI ist nicht installiert; optionale Integration wird übersprungen.");
      return;
    }
    try {
      Class<?> type =
          Class.forName(IMPLEMENTATION, true, LabyModIntegration.class.getClassLoader());
      Constructor<?> constructor =
          type.getConstructor(FlunarBauserver.class, LabyModSettings.class);
      Listener listener = (Listener) constructor.newInstance(plugin, settings);
      Bukkit.getPluginManager().registerEvents(listener, plugin);
      plugin.getLogger().info("Optionale LabyMod-Server-API wurde angebunden.");
    } catch (ReflectiveOperationException | LinkageError exception) {
      Throwable cause =
          exception instanceof InvocationTargetException invocation && invocation.getCause() != null
              ? invocation.getCause()
              : exception;
      plugin
          .getLogger()
          .log(
              Level.WARNING,
              "LabyModServerAPI konnte nicht angebunden werden; Bauserver läuft ohne Integration weiter.",
              cause);
    }
  }
}
