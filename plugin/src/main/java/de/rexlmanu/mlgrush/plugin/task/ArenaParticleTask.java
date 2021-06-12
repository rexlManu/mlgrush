package de.rexlmanu.mlgrush.plugin.task;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import org.bukkit.Bukkit;
import xyz.xenondevs.particle.ParticleEffect;

public class ArenaParticleTask implements Runnable {
  private float radius, angle, height;

  public ArenaParticleTask() {
    this.radius = 2f;
    this.angle = 0;
    this.height = 0;
    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 1);
  }

  @Override
  public void run() {
    GameManager.instance().locationProvider().get("queue-npc").ifPresent(location -> {
      if (this.angle >= 360) this.angle = 0;
      if (this.radius < 0.7f) {
        this.radius = 2f;
        this.height = 0;
      }

      {
        double x = this.radius * Math.sin(this.angle);
        double z = this.radius * Math.cos(this.angle);
        ParticleEffect.SPELL_WITCH.display(location.clone().add(x, this.height, z), 0, 0, 0, 5, 1, null);
      }
      {
        float oppositeAngle = this.angle;
        oppositeAngle += 180;
        if (oppositeAngle > 360) {
          oppositeAngle -= 360;
        }

        double x = this.radius * Math.sin(oppositeAngle);
        double z = this.radius * Math.cos(oppositeAngle);
        ParticleEffect.SPELL_WITCH.display(location.clone().add(x, this.height, z), 0, 0, 0, 5, 1, null);
      }

      this.height += 0.025f;
      this.radius -= 0.01f;
      this.angle += 0.1f;
    });
  }
}
