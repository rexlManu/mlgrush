package de.rexlmanu.mlgrush.plugin.utility.hologram;

import de.rexlmanu.mlgrush.plugin.utility.PlayerUtils;
import eu.miopowered.packetlistener.reflection.PacketReflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rexlManu on 22.07.2017.
 * Plugin by rexlManu
 * https://rexlGames.de
 * Coded with IntelliJ
 */
public class VirtualHologram {
  private Location location;
  private List<String> lines;
  private double distance_above = -0.27D;
  private List<Object> armorstands = new ArrayList<>();

  public VirtualHologram(final Location loc, final String... lines) {
    this.location = loc;
    this.lines = Arrays.asList(lines);
  }

  public VirtualHologram(final Location loc, final List<String> lines) {
    this.location = loc;
    this.lines = lines;
  }

  public List<String> getLines() {
    return this.lines;
  }

  public Location getLocation() {
    return this.location;
  }

  public void send(final Player p) {
    double y = this.getLocation().getY();
    for (int i = 0; i <= this.lines.size() - 1; i++) {
      y += this.distance_above;
      if(this.lines.get(i).isEmpty()) continue;
      final Object armorStand = this.getEntityArmorStand(y);
      if (armorStand == null) continue;
      try {
        armorStand.getClass().getMethod("setCustomName", String.class).invoke(armorStand, this.lines.get(i));
        this.display(p, armorStand);
        this.armorstands.add(armorStand);
      } catch (ReflectiveOperationException e) {
        e.printStackTrace();
      }
    }
  }


  public void setValue(final Object obj, final String name, final Object value) {
    try {
      final Field field = obj.getClass().getDeclaredField(name);
      field.setAccessible(true);
      field.set(obj, value);
    } catch (final Exception localException) {
      localException.printStackTrace();
    }
  }

  public int getFixLocation(final double pos) {
    return (int) Math.floor(pos * 32.0D);
  }

  public byte getFixRotation(final float yawpitch) {
    return (byte) (int) (yawpitch * 256.0F / 360.0F);
  }

  public void destroy(final Player p) {
    try {

      for (final Object armorStand : this.armorstands) {
        Object packet = PacketReflection.nmsClass("PacketPlayOutEntityDestroy")
          .getConstructor(int[].class)
          .newInstance(new int[]{ (int) armorStand.getClass().getMethod("getId").invoke(armorStand) });
        PlayerUtils.sendPacket(p, packet);
      }
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
  }

  public void destroy() {
    for (final Player p : Bukkit.getOnlinePlayers()) {
      this.destroy(p);
    }
  }

  public void broadcast() {
    for (final Player p : Bukkit.getOnlinePlayers()) {
      this.send(p);
    }
  }

  public void broadcast(final List<Player> players) {
    for (final Player p : players) {
      this.send(p);
    }
  }

  private Object getEntityArmorStand(final double y) {
    try {
      World bukkitWorld = this.getLocation().getWorld();
      Object world = bukkitWorld.getClass().getMethod("getHandle").invoke(bukkitWorld);
      Object armorStand = PacketReflection.nmsClass("EntityArmorStand")
        .getConstructor(PacketReflection.nmsClass("World")).newInstance(world);
      armorStand.getClass()
        .getMethod("setLocation", double.class, double.class, double.class, float.class, float.class)
        .invoke(armorStand, this.getLocation().getX(), y, this.getLocation().getZ(), 0.0F, 0.0F);
      armorStand.getClass().getMethod("setInvisible", boolean.class).invoke(armorStand, true);
      armorStand.getClass().getMethod("setCustomNameVisible", boolean.class).invoke(armorStand, true);
      return armorStand;
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void display(final Player p, final Object eas) {
    try {
      Object packet = PacketReflection.nmsClass("PacketPlayOutSpawnEntityLiving")
        .getConstructor(PacketReflection.nmsClass("EntityLiving"))
        .newInstance(eas);
      PlayerUtils.sendPacket(p, packet);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
  }

  public void setArmorstands(final List<Object> armorstands) {
    this.armorstands = armorstands;
  }

  public void setDistance_above(final double distance_above) {
    this.distance_above = distance_above;
  }

  public void setLines(final List<String> lines) {
    this.lines = lines;
  }

  public void setLines(final String... lines) {
    this.lines = Arrays.asList(lines);
  }

  public void setLocation(final Location location) {
    this.location = location;
  }

  public double getDistance_above() {
    return this.distance_above;
  }

  public List<Object> getArmorstands() {
    return this.armorstands;
  }
}

