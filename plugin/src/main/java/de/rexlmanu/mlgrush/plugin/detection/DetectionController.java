package de.rexlmanu.mlgrush.plugin.detection;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import eu.miopowered.packetlistener.PacketHandler;
import eu.miopowered.packetlistener.PacketListener;
import eu.miopowered.packetlistener.filter.PacketState;

public class DetectionController {

  public DetectionController() {
    new DetectionTask();
  }

  public void register(GamePlayer gamePlayer) {
    DetectionPacketHandler handler = new DetectionPacketHandler(gamePlayer);
    PacketHandler
      .listen(PacketListener.of(gamePlayer.player())
        .filter(PacketState.PLAY)
        .receive(handler)
        .sent(handler));
  }

  public void unregister(GamePlayer gamePlayer) {
    PacketHandler.remove(gamePlayer.player());
  }
}
