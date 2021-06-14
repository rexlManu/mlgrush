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
      detection.lastClicks(detection.clicks());
      detection.clickHistory().add(detection.clicks());
      detection.clickAverageSecondly(detection.clickHistory().stream().mapToDouble(Integer::doubleValue).average().orElse(0));
      if (detection.clickHistory().size() >= 10) {
        detection.clickAverage(detection.clickHistory().stream().mapToDouble(Integer::doubleValue).average().getAsDouble());
        detection.clickHistory().clear();
      }
      detection.clicks(0);
      detection.lastPlaces(detection.places());
      detection.placeHistory().add(detection.places());
      detection.placeAverageSecondly(detection.placeHistory().stream().mapToDouble(Integer::doubleValue).average().orElse(0));
      if (detection.placeHistory().size() >= 10) {
        detection.placeAverage(detection.placeHistory().stream().mapToDouble(Integer::doubleValue).average().getAsDouble());
        detection.placeHistory().clear();
      }
      detection.places(0);
    });
  }

}
