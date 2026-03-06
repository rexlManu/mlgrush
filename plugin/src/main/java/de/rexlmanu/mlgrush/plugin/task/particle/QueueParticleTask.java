package de.rexlmanu.mlgrush.plugin.task.particle;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;

public class QueueParticleTask implements Runnable {

  private final Location location;

  private float radius;
  private float angle;
  private float height;

  public QueueParticleTask(Location location) {
    this.location = location;
    this.radius = 2f;
    this.angle = 0;
    this.height = 0;

    Bukkit.getScheduler().runTaskTimer(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 1);
  }

  @Override
  public void run() {
    if (this.angle >= 360) {
      this.angle = 0;
    }
    if (this.radius < 0.7f) {
      this.radius = 2f;
      this.height = 0;
    }

    this.spawnAtAngle(this.angle);

    float oppositeAngle = this.angle + 180;
    if (oppositeAngle > 360) {
      oppositeAngle -= 360;
    }
    this.spawnAtAngle(oppositeAngle);

    this.height += 0.025f;
    this.radius -= 0.01f;
    this.angle += 0.1f;
  }

  private void spawnAtAngle(float currentAngle) {
    double x = this.radius * Math.sin(currentAngle);
    double z = this.radius * Math.cos(currentAngle);
    this.location.getWorld().spawnParticle(Particle.WITCH, this.location.clone().add(x, this.height, z), 1, 0, 0, 0, 0);
  }
}
