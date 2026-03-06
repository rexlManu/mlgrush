package de.rexlmanu.mlgrush.plugin.task.particle;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.List;

public class FloorParticleTask implements Runnable {

  private final List<Location> cornerLocations = new ArrayList<>();
  private int step;

  public FloorParticleTask(Location location) {
    this.step = 0;
    Bukkit.getScheduler().runTaskTimer(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 1);

    Location corner = location.clone().subtract(1.5, 0, 1.5);
    for (double i = 0; i < 3; i += 0.1D) {
      this.cornerLocations.add(corner.clone().add(i, 0, 0));
    }
    for (double i = 0; i < 3; i += 0.1D) {
      this.cornerLocations.add(corner.clone().add(3, 0, i));
    }
    for (double i = 3; i > 0; i -= 0.1D) {
      this.cornerLocations.add(corner.clone().add(i, 0, 3));
    }
    for (double i = 3; i > 0; i -= 0.1D) {
      this.cornerLocations.add(corner.clone().add(0, 0, i));
    }
  }

  @Override
  public void run() {
    if (this.cornerLocations.isEmpty()) {
      return;
    }
    if (this.cornerLocations.size() <= this.step + 1) {
      this.step = 0;
    }

    Location location = this.cornerLocations.get(this.step);
    location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0.001D);
    this.step++;
  }
}
