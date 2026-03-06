package de.rexlmanu.mlgrush.plugin.task.particle;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;

@Accessors(fluent = true)
public class TwinsParticleTask implements Runnable {

  private int stepX = 0;
  private int stepY = 0;
  private boolean reverse = false;

  private final int orbs = 2;
  private final double radius = 1;
  private final int numSteps = 60;
  private final int maxStepY = 30;
  private final Location location;

  public TwinsParticleTask(Location location) {
    this.location = location.clone().add(0, 1, 0);

    Bukkit.getScheduler().runTaskTimer(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 1);
  }

  @Override
  public void run() {
    for (int i = 0; i < this.orbs; i++) {
      double slice = Math.PI * 2 / this.numSteps;
      double orbSlice = Math.PI * 2 / this.orbs;

      double dx = -Math.cos(slice * this.stepX + orbSlice * i) * this.radius;
      double dy = this.stepY / (double) this.maxStepY;
      double dz = -Math.sin(slice * this.stepX + orbSlice * i) * this.radius;

      this.location
          .getWorld()
          .spawnParticle(Particle.FLAME, this.location.clone().add(dx, dy, dz), 1, 0, 0, 0, 0.01D);
    }

    this.stepX++;
    if (this.stepX > this.numSteps) {
      this.stepX = 0;
    }

    if (this.reverse) {
      this.stepY++;
      if (this.stepY > this.maxStepY) {
        this.reverse = false;
      }
    } else {
      this.stepY--;
      if (this.stepY < -this.maxStepY) {
        this.reverse = true;
      }
    }
  }
}
