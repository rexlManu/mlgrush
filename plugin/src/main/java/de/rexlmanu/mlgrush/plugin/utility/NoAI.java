package de.rexlmanu.mlgrush.plugin.utility;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public final class NoAI {

  private NoAI() {}

  public static void setEntityAi(Entity entity, boolean ai) {
    if (entity instanceof LivingEntity livingEntity) {
      livingEntity.setAI(ai);
    }
    if (entity instanceof Mob mob) {
      mob.setAware(ai);
    }
    entity.setSilent(!ai);
  }
}
