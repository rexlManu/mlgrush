package de.rexlmanu.mlgrush.plugin.detection;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import eu.miopowered.packetlistener.context.PacketReceive;
import eu.miopowered.packetlistener.context.PacketSent;
import eu.miopowered.packetlistener.reflection.PacketReflection;
import eu.miopowered.packetlistener.reflection.WrappedPacket;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public class DetectionPacketHandler implements PacketReceive, PacketSent {
  private GamePlayer gamePlayer;

  @Override
  public void handle(ChannelHandlerContext context, WrappedPacket packet) {
    Detection detection = gamePlayer.detection();
    switch (packet.packetName()) {
      case "PacketPlayInBlockDig":
        String type = this.getDigType(packet.packet());
        switch (type) {
          case "START_DESTROY_BLOCK":
            detection.digging(true);
            break;
          case "ABORT_DESTROY_BLOCK":
          case "STOP_DESTROY_BLOCK":
            detection.digging(false);
            detection.lastDiggingAction(System.currentTimeMillis());
            break;
          default:
            break;
        }
        break;
      case "PacketPlayInArmAnimation":
        if (!detection.digging() || System.currentTimeMillis() - detection.lastDiggingAction() > 1000L) {
          detection.clicks(detection.clicks() + 1);
        }
        break;
      case "PacketPlayInTransaction":
        try {
          Field b = packet.packet().getClass().getField("b");
          b.setAccessible(true);
          if (((short) b.get(packet.packet())) == detection.transactionId()) {
            detection.lastTransactionPing(System.currentTimeMillis() - detection.startTransactionPing());
          }
        } catch (ReflectiveOperationException e) {
          e.printStackTrace();
        }
        break;
      case "PacketPlayOutKeepAlive":
        detection.startTransactionPing(System.currentTimeMillis());
        detection.transactionId(ThreadLocalRandom.current().nextInt(1000, Short.MAX_VALUE));
        try {
          Player player = gamePlayer.player();
          Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
          Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
          Method sendPacket = playerConnection.getClass().getMethod("sendPacket", PacketReflection.nmsClass("Packet"));
          Object packetPlayOutTransaction = PacketReflection.nmsClass("PacketPlayOutTransaction")
            .getConstructor(int.class, short.class, boolean.class)
            .newInstance(0, (short) detection.transactionId(), false);
          sendPacket.invoke(playerConnection, packetPlayOutTransaction);
        } catch (Exception e) {
          e.printStackTrace();
        }
        break;
      default:
        break;
    }
  }

  private String getDigType(Object packet) {
    try {
      Field c = packet.getClass().getField("c");
      c.setAccessible(true);
      return (String) c.get(packet).getClass().getMethod("name").invoke(c);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
      return "";
    }
  }
}
