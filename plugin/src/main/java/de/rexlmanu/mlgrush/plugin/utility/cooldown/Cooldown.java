package de.rexlmanu.mlgrush.plugin.utility.cooldown;

import net.jodah.expiringmap.ExpiringMap;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Cooldown {
  private Map<UUID, Long> playersCooldown;
  private long maximalCooldown;

  public Cooldown(long maximalCooldown) {
    this.playersCooldown = ExpiringMap
      .builder()
      .expiration(maximalCooldown, TimeUnit.MILLISECONDS)
      .build();
    this.maximalCooldown = maximalCooldown;
  }

  public void add(UUID uuid) {
    this.playersCooldown.put(uuid, System.currentTimeMillis() + maximalCooldown);
  }

  public boolean expired(UUID uuid) {
    return System.currentTimeMillis() > this.playersCooldown.getOrDefault(uuid, 0L);
  }

  public boolean currently(UUID uuid) {
    return !this.expired(uuid);
  }

  public void remove(UUID uuid) {
    this.playersCooldown.remove(uuid);
  }
}
