package de.rexlmanu.mlgrush.plugin.task.particle;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.ArrayList;
import java.util.List;

public class FloorParticleTask implements Runnable {

  private Location location;

  private List<Location> cornerLocations;
  private int step;

  public FloorParticleTask(Location location) {
    this.location = location;
    this.cornerLocations = new ArrayList<>();
    this.step = 0;
    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 1);

    Location corner = location.clone().subtract(1.5, 0, 1.5);
    for (double i = 0; i < 3; i += 1d / 10d) {
      cornerLocations.add(corner.clone().add(i, 0, 0));
    }
    for (double i = 0; i < 3; i += 1d / 10d) {
      cornerLocations.add(corner.clone().add(3, 0, i));
    }
    for (double i = 3; i > 0; i -= 1d / 10d) {
      cornerLocations.add(corner.clone().add(i, 0, 3));
    }
    for (double i = 3; i > 0; i -= 1d / 10d) {
      cornerLocations.add(corner.clone().add(0, 0, i));
    }
  }

  @Override
  public void run() {
    if (this.cornerLocations.size() <= this.step) this.step = 0;

    ParticleEffect.FLAME.display(this.cornerLocations.get(this.step), 0, 0, 0, 0.001f, 1, null);

    this.step++;
  }
}
