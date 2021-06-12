package de.rexlmanu.mlgrush.plugin.task;

import com.cryptomorin.xseries.messages.ActionBar;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import org.bukkit.Bukkit;

public class ArenaActionbarTask implements Runnable {

  public ArenaActionbarTask() {
    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 1);
  }

  @Override
  public void run() {
    GameManager.instance().arenaManager().arenaContainer().activeArenas().forEach(arena -> {
      long seconds = (System.currentTimeMillis() - arena.gameStart()) / 1000;
      StringBuilder prefix = new StringBuilder();
      StringBuilder suffix = new StringBuilder();
      int halfTeamCount = arena.gameTeams().size() / 2;
      for (int i = 0; i < arena.gameTeams().size(); i++) {
        GameTeam team = arena.gameTeams().get(i);
        if (halfTeamCount < (i + 1)) {
          suffix.append(team.name().color()).append(team.points());
        } else {
          prefix.append(team.name().color()).append(team.points());
        }
      }
      String message = MessageFormat.replaceColors(
        String.format(
          "%s &8■ &7%02d:%02d:%02d &8■ %s",
          prefix.toString(),
          seconds / 3600, (seconds % 3600) / 60, seconds % 60,
          suffix.toString()
        )
      );
      arena.players().stream().filter(gamePlayer -> gamePlayer.player() != null).forEach(gamePlayer -> ActionBar.sendActionBar(gamePlayer.player(), message));
      arena.spectators().forEach(gamePlayer -> ActionBar.sendActionBar(gamePlayer.player(), message));
    });
  }
}
