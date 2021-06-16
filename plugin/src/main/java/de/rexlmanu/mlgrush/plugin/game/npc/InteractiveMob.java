package de.rexlmanu.mlgrush.plugin.game.npc;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.NoAI;
import de.rexlmanu.mlgrush.plugin.utility.hologram.Hologram;
import eu.miopowered.packetlistener.reflection.PacketReflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;

public class InteractiveMob implements Runnable, Listener {

  private Entity entity;
  private BukkitTask viewTask;
  private Hologram hologram;

  private final EntityType type;
  private List<String> lines;
  private final InteractionHandler listener;

  public InteractiveMob(EntityType type, List<String> lines, InteractionHandler listener) {
    this.type = type;
    this.lines = lines;
    this.listener = listener;
  }

  public InteractiveMob create(Location location) {
    this.entity = location.getWorld().spawnEntity(location, this.type);
    NoAI.setEntityAi(this.entity, false);
    this.hologram = new Hologram(this.lines, location.clone().add(0, 0.6, 0));

    JavaPlugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
    this.viewTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0, 1);
    Bukkit.getPluginManager().registerEvents(this, plugin);
    return this;
  }

  public void remove() {
    this.viewTask.cancel();
    this.entity.remove();
    this.hologram.delete();
    HandlerList.unregisterAll(this);
  }

  @Override
  public void run() {
    Bukkit.getOnlinePlayers()
      .stream()
      .filter(player -> player.getLocation().getWorld().equals(entity.getWorld()))
      .filter(player -> player.getLocation().distance(entity.getLocation()) < 5)
      .forEach(player -> {
        Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
        try {
          Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
          Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);

          Object entityLookPacket = PacketReflection.nmsClass("PacketPlayOutEntity$PacketPlayOutEntityLook")
            .getConstructor(int.class, byte.class, byte.class, boolean.class
            ).newInstance(
              this.entity.getEntityId(),
              toPackedByte((float) (180 - Math.toDegrees(Math.atan2(direction.getX(), direction.getZ())))),
              toPackedByte((float) (90 - Math.toDegrees(Math.acos(direction.getY())))),
              true
            );
          Location location = entity.getLocation();
          location.setDirection(direction);
          location.setYaw((float) (180 - Math.toDegrees(Math.atan2(direction.getX(), direction.getZ()))));
          location.setPitch((float) (90 - Math.toDegrees(Math.acos(direction.getY()))));
          Object packetPlayOutEntityTeleport = PacketReflection.nmsClass("PacketPlayOutEntityTeleport")
            .getConstructor(int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class)
            .newInstance(
              this.entity.getEntityId(),
              floor(location.getX() * 32D),
              floor(location.getY() * 32D),
              floor(location.getZ() * 32D),
              toPackedByte(location.getYaw()),
              toPackedByte(location.getPitch()),
              true
            );
          playerConnection.getClass().getMethod("sendPacket", PacketReflection.nmsClass("Packet")).invoke(playerConnection, packetPlayOutEntityTeleport);
          playerConnection.getClass().getMethod("sendPacket", PacketReflection.nmsClass("Packet")).invoke(playerConnection, entityLookPacket);

        } catch (Exception e) {
          e.printStackTrace();
        }
      });
  }

  @EventHandler
  public void handle(PlayerInteractEntityEvent event) {
    if (event.getRightClicked().equals(this.entity)) {
      event.setCancelled(true);
      PlayerProvider.find(event.getPlayer().getUniqueId()).ifPresent(this.listener::handle);
    }
  }

  private byte toPackedByte(float f) {
    return (byte) ((int) (f * 256.0F / 360.0F));
  }

  public static int floor(double var0) {
    int var2 = (int) var0;
    return var0 < (double) var2 ? var2 - 1 : var2;
  }

}
