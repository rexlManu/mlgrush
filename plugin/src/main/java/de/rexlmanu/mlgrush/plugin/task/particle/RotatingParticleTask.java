package de.rexlmanu.mlgrush.plugin.task.particle;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

@Accessors(fluent = true)
public class RotatingParticleTask implements Runnable {
  private final Location location;
  private int degreee = 0;

  public RotatingParticleTask() {
    this.location = new Location(Bukkit.getWorld("world"), 0.5, 68, 0.5);

    Bukkit.getScheduler().runTaskTimer(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 2);
  }

  @Override
  public void run() {
    if (this.location.getWorld() == null) {
      return;
    }
    if (this.degreee > 360) {
      this.degreee = 0;
    }
    double xangle = Math.toRadians(this.degreee);
    double xAxisCos = Math.cos(xangle);
    double xAxisSin = Math.sin(xangle);

    int yDegree = this.degreee + 45;
    if (yDegree > 360) {
      yDegree -= 360;
    }
    double yangle = Math.toRadians(yDegree);
    double yAxisCos = Math.cos(-yangle);
    double yAxisSin = Math.sin(-yangle);

    int zDegree = this.degreee + 90;
    if (zDegree > 360) {
      zDegree -= 360;
    }
    double zangle = Math.toRadians(zDegree);
    double zAxisCos = Math.cos(zangle);
    double zAxisSin = Math.sin(zangle);

    double radius = 1.4f;

    for (int degree = 0; degree < 360; degree += 8) {
      double radians = Math.toRadians(degree);
      double x = radius * Math.cos(radians);
      double z = radius * Math.sin(radians);
      Vector vector = new Vector(x, 0, z);
      rotateAroundAxisX(vector, xAxisCos, xAxisSin);
      rotateAroundAxisY(vector, yAxisCos, yAxisSin);
      rotateAroundAxisZ(vector, zAxisCos, zAxisSin);

      this.location.getWorld().spawnParticle(Particle.FLAME, this.location.clone().add(vector), 1, 0, 0, 0, 0.01D);
    }

    this.degreee += 3;
  }

  private Vector rotateAroundAxisX(Vector vector, double cos, double sin) {
    double y = vector.getY() * cos - vector.getZ() * sin;
    double z = vector.getY() * sin + vector.getZ() * cos;
    return vector.setY(y).setZ(z);
  }

  private Vector rotateAroundAxisY(Vector vector, double cos, double sin) {
    double x = vector.getX() * cos + vector.getZ() * sin;
    double z = vector.getX() * -sin + vector.getZ() * cos;
    return vector.setX(x).setZ(z);
  }

  private Vector rotateAroundAxisZ(Vector vector, double cos, double sin) {
    double x = vector.getX() * cos - vector.getY() * sin;
    double y = vector.getX() * sin + vector.getY() * cos;
    return vector.setX(x).setY(y);
  }
}
