package de.rexlmanu.mlgrush.plugin.task;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.stats.StatsHandler;
import org.bukkit.Bukkit;

public class UpdateStatsWallTask implements Runnable {
  public UpdateStatsWallTask() {
    StatsHandler.updateStatsWall();
    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 10 * 60 * 20);
  }

  @Override
  public void run() {
    StatsHandler.updateStatsWall();
  }
}
