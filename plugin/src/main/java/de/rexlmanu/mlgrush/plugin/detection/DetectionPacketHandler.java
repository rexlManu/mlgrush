package de.rexlmanu.mlgrush.plugin.detection;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import eu.miopowered.packetlistener.context.PacketReceive;
import eu.miopowered.packetlistener.context.PacketSent;
import eu.miopowered.packetlistener.reflection.PacketReflection;
import eu.miopowered.packetlistener.reflection.WrappedPacket;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;

public class DetectionPacketHandler implements PacketReceive, PacketSent {
  private GamePlayer gamePlayer;

  public DetectionPacketHandler(GamePlayer gamePlayer) {
    this.gamePlayer = gamePlayer;
  }

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
        if (detection.placing()) {
          detection.placing(false);
          break;
        }
        if (!detection.digging() && System.currentTimeMillis() - detection.lastDiggingAction() > 1000L) {
          detection.clicks(detection.clicks() + 1);
        }
        break;
      case "PacketPlayInTransaction":
        try {
          if ((short) this.getPrivateField(packet.packet(), "b") == detection.transactionId()) {
            detection.transactionPing(System.currentTimeMillis() - detection.startTransactionTime());
          }
        } catch (ReflectiveOperationException e) {
          e.printStackTrace();
        }
        break;
      case "PacketPlayInBlockPlace":
        try {
          detection.places(detection.places() + 1);

          Object blockPosition = this.getPrivateField(packet.packet(), "b");
          int x = (int) this.getPrivateField(PacketReflection.nmsClass("BaseBlockPosition"), blockPosition, "a");
          int y = (int) this.getPrivateField(PacketReflection.nmsClass("BaseBlockPosition"), blockPosition, "c");
          int z = (int) this.getPrivateField(PacketReflection.nmsClass("BaseBlockPosition"), blockPosition, "d");
          if (x == -1 && y == -1 && z == -1) break; // Thats a item not a block :)
          detection.placing(true);
        } catch (ReflectiveOperationException e) {
          e.printStackTrace();
        }
        break;
      case "PacketPlayOutKeepAlive":
        detection.startTransactionTime(System.currentTimeMillis());
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

  private Object getPrivateField(Class<?> targetClass, Object object, String name) throws NoSuchFieldException, IllegalAccessException {
    Field declaredField = targetClass.getDeclaredField(name);
    declaredField.setAccessible(true);
    return declaredField.get(object);
  }

  private Object getPrivateField(Object object, String name) throws NoSuchFieldException, IllegalAccessException {
    Field declaredField = object.getClass().getDeclaredField(name);
    declaredField.setAccessible(true);
    return declaredField.get(object);
  }

  private String getDigType(Object packet) {
    try {
      Object enumValue = this.getPrivateField(packet, "c");
      return (String) enumValue.getClass().getMethod("name").invoke(enumValue);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
      return "";
    }
  }
}
