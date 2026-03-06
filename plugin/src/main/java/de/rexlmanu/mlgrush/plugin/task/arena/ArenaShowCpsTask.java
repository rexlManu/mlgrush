package de.rexlmanu.mlgrush.plugin.task.arena;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;

public class ArenaShowCpsTask implements Runnable {
  private static final String OBJECTIVE_TITLE = "&acps";

  public ArenaShowCpsTask() {
    Bukkit.getScheduler()
        .runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 6);
  }

  @Override
  public void run() {
    GameManager.instance().arenaManager().arenaContainer().activeArenas().stream()
        .filter(arena -> arena.configuration().showCps())
        .forEach(
            arena -> {
              Map<String, Integer> scores = new HashMap<>();
              arena
                  .players()
                  .forEach(
                      target ->
                          scores.put(
                              target.player().getName(),
                              (int) target.detection().clickAverageSecondly()));
              arena
                  .players()
                  .forEach(
                      gamePlayer -> {
                        gamePlayer.scoreboardSession().updateBelowName(OBJECTIVE_TITLE, scores);
                      });
            });
  }
}
