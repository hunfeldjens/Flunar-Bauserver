package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import org.bukkit.entity.Player;

final class ProjectHelpSubcommand extends AbstractProjectSubcommand {
  ProjectHelpSubcommand(BauserverContext c) {
    super(c);
  }

  public void execute(Player p, String[] a) {
    context.messages().send(p, "<green>/projekt <gray>– Übersicht");
    context
        .messages()
        .send(p, "<green>/projekt info|create|remove|tp|whitelist|ban|unban|autoload|export");
  }
}
