package de.rexlmanu.mlgrush.plugin.detection;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import org.bukkit.entity.Player;

public class DetectionPacketListener extends PacketListenerAbstract {

  public DetectionPacketListener() {
    super(PacketListenerPriority.NORMAL);
  }

  @Override
  public void onPacketReceive(PacketReceiveEvent event) {
    Object source = event.getPlayer();
    if (!(source instanceof Player player)) {
      return;
    }

    PlayerProvider.find(player.getUniqueId()).ifPresent(gamePlayer -> this.handleReceive(gamePlayer, event));
  }

  @Override
  public void onPacketSend(PacketSendEvent event) {
    Object source = event.getPlayer();
    if (!(source instanceof Player player)) {
      return;
    }

    PlayerProvider.find(player.getUniqueId()).ifPresent(gamePlayer -> {
      if (event.getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
        WrapperPlayServerKeepAlive wrapper = new WrapperPlayServerKeepAlive(event);
        Detection detection = gamePlayer.detection();
        detection.startTransactionTime(System.currentTimeMillis());
        detection.transactionId(wrapper.getId());
      }
    });
  }

  private void handleReceive(GamePlayer gamePlayer, PacketReceiveEvent event) {
    Detection detection = gamePlayer.detection();
    if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
      WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
      DiggingAction action = wrapper.getAction();
      if (action == DiggingAction.START_DIGGING) {
        detection.digging(true);
        return;
      }
      if (action == DiggingAction.CANCELLED_DIGGING || action == DiggingAction.FINISHED_DIGGING) {
        detection.digging(false);
        detection.lastDiggingAction(System.currentTimeMillis());
      }
      return;
    }

    if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
      new WrapperPlayClientAnimation(event);
      if (detection.placing()) {
        detection.placing(false);
        return;
      }
      if (!detection.digging() && System.currentTimeMillis() - detection.lastDiggingAction() > 1000L) {
        detection.clicks(detection.clicks() + 1);
      }
      return;
    }

    if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
      WrapperPlayClientPlayerBlockPlacement wrapper = new WrapperPlayClientPlayerBlockPlacement(event);
      if (wrapper.getBlockPosition() != null) {
        detection.places(detection.places() + 1);
        detection.placing(true);
        detection.lastPlacingAction(System.currentTimeMillis());
      }
      return;
    }

    if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
      WrapperPlayClientKeepAlive wrapper = new WrapperPlayClientKeepAlive(event);
      if (wrapper.getId() == detection.transactionId()) {
        detection.transactionPing(System.currentTimeMillis() - detection.startTransactionTime());
      }
    }
  }
}
