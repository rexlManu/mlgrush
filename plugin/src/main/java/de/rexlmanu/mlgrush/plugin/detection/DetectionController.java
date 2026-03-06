package de.rexlmanu.mlgrush.plugin.detection;

import com.github.retrooper.packetevents.PacketEvents;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;

public class DetectionController {

  public DetectionController() {
    PacketEvents.getAPI().getEventManager().registerListener(new DetectionPacketListener());
    new DetectionTask();
  }

  public void register(GamePlayer gamePlayer) {
    if (gamePlayer.player() != null) {
      gamePlayer.detection().transactionPing(PacketEvents.getAPI().getPlayerManager().getPing(gamePlayer.player()));
    }
  }

  public void unregister(GamePlayer gamePlayer) {
    gamePlayer.detection().digging(false).placing(false);
  }
}
