package eu.hunfeld.flunarBauserver.commands.project;

import java.util.List;
import org.bukkit.entity.Player;

public interface ProjectSubcommand {
  void execute(Player player, String[] args);

  default List<String> tabComplete(Player player, String[] args) {
    return List.of();
  }
}
