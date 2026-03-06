package de.rexlmanu.mlgrush.plugin.utility.hologram;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VirtualHologram {
  private Location location;
  private List<String> lines;
  private double distanceAbove = -0.27D;
  private final List<ArmorStand> armorStands = new ArrayList<>();
  private final Set<UUID> viewers = new HashSet<>();

  public VirtualHologram(Location location, String... lines) {
    this(location, Arrays.asList(lines));
  }

  public VirtualHologram(Location location, List<String> lines) {
    this.location = location;
    this.lines = new ArrayList<>(lines);
  }

  public List<String> getLines() {
    return new ArrayList<>(this.lines);
  }

  public Location getLocation() {
    return this.location;
  }

  public void send(Player player) {
    if (player == null) {
      return;
    }
    Plugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
    this.ensureSpawned();
    this.viewers.add(player.getUniqueId());
    this.armorStands.forEach(armorStand -> player.showEntity(plugin, armorStand));
  }

  public void destroy(Player player) {
    if (player == null) {
      return;
    }
    Plugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
    this.armorStands.forEach(armorStand -> player.hideEntity(plugin, armorStand));
    this.viewers.remove(player.getUniqueId());
    if (this.viewers.isEmpty()) {
      this.destroy();
    }
  }

  public void destroy() {
    this.armorStands.forEach(ArmorStand::remove);
    this.armorStands.clear();
    this.viewers.clear();
  }

  public void broadcast() {
    Bukkit.getOnlinePlayers().forEach(this::send);
  }

  public void broadcast(List<Player> players) {
    players.forEach(this::send);
  }

  public void setDistance_above(double distanceAbove) {
    this.distanceAbove = distanceAbove;
  }

  public void setLines(List<String> lines) {
    this.lines = new ArrayList<>(lines);
    this.reset();
  }

  public void setLines(String... lines) {
    this.setLines(Arrays.asList(lines));
  }

  public void setLocation(Location location) {
    this.location = location;
    this.reset();
  }

  public double getDistance_above() {
    return this.distanceAbove;
  }

  private void ensureSpawned() {
    if (!this.armorStands.isEmpty()) {
      return;
    }

    double y = this.location.getY();
    Plugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
    for (String line : this.lines) {
      y += this.distanceAbove;
      if (line.isEmpty()) {
        continue;
      }
      ArmorStand armorStand = (ArmorStand) this.location.getWorld().spawnEntity(new Location(this.location.getWorld(), this.location.getX(), y, this.location.getZ()), EntityType.ARMOR_STAND);
      armorStand.setInvisible(true);
      armorStand.setCustomNameVisible(true);
      armorStand.setCustomName(line);
      armorStand.setMarker(true);
      armorStand.setGravity(false);
      armorStand.setPersistent(false);
      armorStand.setVisibleByDefault(false);
      Bukkit.getOnlinePlayers().forEach(target -> target.hideEntity(plugin, armorStand));
      this.armorStands.add(armorStand);
    }
  }

  private void reset() {
    Set<UUID> oldViewers = new HashSet<>(this.viewers);
    this.destroy();
    oldViewers.stream().map(Bukkit::getPlayer).forEach(this::send);
  }
}
