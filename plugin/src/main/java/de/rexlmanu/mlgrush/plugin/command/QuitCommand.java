package de.rexlmanu.mlgrush.plugin.command;

import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuitCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) return true;
    PlayerProvider.find(((Player) sender).getUniqueId()).ifPresent(gamePlayer -> {
      if (!gamePlayer.environment().equals(Environment.ARENA)) {
        gamePlayer.sendMessage("Du befindest dich in keinem Spiel.");
        return;
      }
      GameManager.instance().arenaManager().arenaContainer().findArenaByPlayer(gamePlayer).ifPresent(arena -> {
        arena.players().forEach(target -> target.sendMessage(String.format("&e%s &7hat das Spiel verlassen.", gamePlayer.player().getName())));
        gamePlayer.sendMessage("Du hast das Spiel verlassen.");
        GameManager.instance().arenaManager().delete(arena);
      });
    });
    return true;
  }
}
