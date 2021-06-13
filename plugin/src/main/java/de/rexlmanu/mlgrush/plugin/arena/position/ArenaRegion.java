package de.rexlmanu.mlgrush.plugin.arena.position;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

@Accessors(fluent = true)
@Data
public class ArenaRegion {

  private int minX, minY, minZ;
  private int maxX, maxY, maxZ;

  private Location firstPoint, secondPoint;

  public ArenaRegion(Location firstPoint, Location secondPoint) {
    this.firstPoint = firstPoint;
    this.secondPoint = secondPoint;

    this.calculateRange();
  }

  private void calculateRange() {
    this.maxX = Math.max(this.firstPoint.getBlockX(), this.secondPoint.getBlockX());
    this.maxY = Math.max(this.firstPoint.getBlockY(), this.secondPoint.getBlockY());
    this.maxZ = Math.max(this.firstPoint.getBlockZ(), this.secondPoint.getBlockZ());

    this.minX = Math.min(this.firstPoint.getBlockX(), this.secondPoint.getBlockX());
    this.minY = Math.min(this.firstPoint.getBlockY(), this.secondPoint.getBlockY());
    this.minZ = Math.min(this.firstPoint.getBlockZ(), this.secondPoint.getBlockZ());
  }

  public boolean contains(Location location) {
    return location.getX() > minX && location.getX() < maxX
      && location.getY() > minY && location.getY() < 250 // replaced from maxY because we take full max
      && location.getZ() > minZ && location.getZ() < maxZ;
  }

  public void clear() {
    Bukkit.getScheduler().runTask(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> {
      for (int x = 0; x < (maxX - minX); x++) {
        for (int y = 0; y < (maxY - minY); y++) {
          for (int z = 0; z < (maxZ - minZ); z++) {
            Location add = this.firstPoint.clone().add(x, y, z);
            if (!add.getBlock().getType().equals(Material.AIR)) {
              add.getBlock().setType(Material.AIR);
            }
          }
        }
      }
    });
  }
}
