package de.rexlmanu.mlgrush.plugin.task.particle;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;

@Accessors(fluent = true)
public class RotatingParticleTask implements Runnable {
  private Location location;
  private int degreee = 0;

  public RotatingParticleTask() {
    this.location = new Location(Bukkit.getWorld("world"), 0.5, 68, 0.5);

    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 2);
  }

  @Override
  public void run() {
    if (degreee > 360) degreee = 0;
    // the numbers are the angles on which you want to rotate your animation.
    double xangle = Math.toRadians(degreee); // note that here we do have to convert to radians.
    double xAxisCos = Math.cos(xangle); // getting the cos value for the pitch.
    double xAxisSin = Math.sin(xangle); // getting the sin value for the pitch.

// DON'T FORGET THE ' - ' IN FRONT OF 'yangle' HERE.
    int yDegree = degreee + 45;
    if (yDegree > 360) yDegree -= 360;
    double yangle = Math.toRadians(yDegree); // note that here we do have to convert to radians.
    double yAxisCos = Math.cos(-yangle); // getting the cos value for the yaw.
    double yAxisSin = Math.sin(-yangle); // getting the sin value for the yaw.

    int zDegree = degreee + 90;
    if (zDegree > 360) zDegree -= 360;
    double zangle = Math.toRadians(zDegree); // note that here we do have to convert to radians.
    double zAxisCos = Math.cos(zangle); // getting the cos value for the roll.
    double zAxisSin = Math.sin(zangle); // getting the sin value for the roll.

    double radius = 1.4f;

    for (int degree = 0; degree < 360; degree += 8) {
      double radians = Math.toRadians(degree);
      double x = radius * Math.cos(radians);
      double z = radius * Math.sin(radians);
//      location.add(x,0,z);
      Vector vec = new Vector(x, 0, z);
      rotateAroundAxisX(vec, xAxisCos, xAxisSin);
      rotateAroundAxisY(vec, yAxisCos, yAxisSin);
      rotateAroundAxisZ(vec, zAxisCos, zAxisSin);

      ParticleEffect.FLAME.display(location.clone().add(vec), 0, 0, 0, 0.01f, 1, null);
//      location.subtract(x,0,z);
    }

    degreee += 3;
  }

  private Vector rotateAroundAxisX(Vector v, double cos, double sin) {
    double y = v.getY() * cos - v.getZ() * sin;
    double z = v.getY() * sin + v.getZ() * cos;
    return v.setY(y).setZ(z);
  }

  private Vector rotateAroundAxisY(Vector v, double cos, double sin) {
    double x = v.getX() * cos + v.getZ() * sin;
    double z = v.getX() * -sin + v.getZ() * cos;
    return v.setX(x).setZ(z);
  }

  private Vector rotateAroundAxisZ(Vector v, double cos, double sin) {
    double x = v.getX() * cos - v.getY() * sin;
    double y = v.getX() * sin + v.getY() * cos;
    return v.setX(x).setY(y);
  }
}
