package de.rexlmanu.mlgrush.plugin.task.arena;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ArenaShowCpsTask implements Runnable {
  private static final String OBJECTIVE_NAME = "show-cps";

  public ArenaShowCpsTask() {
    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 6);
  }

  @Override
  public void run() {
    GameManager
      .instance()
      .arenaManager()
      .arenaContainer()
      .activeArenas()
      .stream()
      .filter(arena -> arena.configuration().showCps())
      .forEach(arena -> {
        arena.players().forEach(gamePlayer -> {
          Player player = gamePlayer.player();
          assert player != null;
          Scoreboard scoreboard = player.getScoreboard();
          if (scoreboard == null) return;
          Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
          if (objective == null) {
            objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy");
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            objective.setDisplayName(MessageFormat.replaceColors("&dcps"));
          }
          Objective finalObjective = objective;
          arena.players().forEach(target -> {
            finalObjective.getScore(target.player().getName()).setScore((int) target.detection().clickAverageSecondly());
          });
        });
      });
  }
}
