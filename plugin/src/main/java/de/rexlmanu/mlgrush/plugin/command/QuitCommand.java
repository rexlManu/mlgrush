package de.rexlmanu.mlgrush.plugin.command;

import de.rexlmanu.mlgrush.plugin.arena.Arena;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class QuitCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) return true;
    PlayerProvider.find(((Player) sender).getUniqueId()).ifPresent(gamePlayer -> {
      if (!gamePlayer.environment().equals(Environment.ARENA)) {
        Optional<Arena> any = GameManager
          .instance()
          .arenaManager()
          .arenaContainer()
          .activeArenas()
          .stream()
          .filter(arena -> arena.spectators().contains(gamePlayer)).findAny();
        any
          .ifPresent(arena -> GameManager.instance().arenaManager().removeSpectator(gamePlayer));
        if (!any.isPresent()) {
          gamePlayer.sendMessage("Du befindest dich in keinem Spiel.");
        }
        return;
      }
      GameManager.instance().arenaManager().arenaContainer().findArenaByPlayer(gamePlayer).ifPresent(arena -> {
        arena.getTeam(gamePlayer).members().remove(gamePlayer);
        arena.players().forEach(target -> target.sendMessage(String.format("&e%s &7hat das Spiel verlassen.", gamePlayer.player().getName())));
        GameManager.instance().arenaManager().delete(arena);
      });
    });
    return true;
  }
}
