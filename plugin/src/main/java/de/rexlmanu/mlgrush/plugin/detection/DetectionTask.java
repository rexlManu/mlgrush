package de.rexlmanu.mlgrush.plugin.detection;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import org.bukkit.Bukkit;

public class DetectionTask implements Runnable {
  public DetectionTask() {
    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 20);
  }

  @Override
  public void run() {
    PlayerProvider.PLAYERS.forEach(gamePlayer -> {
      Detection detection = gamePlayer.detection();
      if (detection.clicks() < 2) return;
      detection.lastClicks(detection.clicks());
      detection.averageSecondly(detection.clickHistory().stream().mapToDouble(Integer::doubleValue).average().getAsDouble());
      detection.clickHistory().add(detection.clicks());
      if (detection.clickHistory().size() >= 10) {
        detection.average(detection.clickHistory().stream().mapToDouble(Integer::doubleValue).average().getAsDouble());
        detection.standardDeviation(detection.clickHistory().stream()
          .map(clicks -> Math.pow(Math.abs(clicks - detection.average()), 2.0))
          .reduce(Double::sum)
          .map(sum -> Math.sqrt(sum / detection.clickHistory().size())).get());
        detection.clickHistory().clear();
      }
      detection.clicks(0);
    });
  }

}
