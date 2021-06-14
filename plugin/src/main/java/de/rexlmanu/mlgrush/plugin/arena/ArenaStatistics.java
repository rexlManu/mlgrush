package de.rexlmanu.mlgrush.plugin.arena;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
@Data
public class ArenaStatistics {
  private int kills, deaths, blocks, destroyedBeds;

  public ArenaStatistics() {
    this.kills = 0;
    this.deaths = 0;
    this.blocks = 0;
    this.destroyedBeds = 0;
  }

  public void addBlock() {
    this.blocks++;
  }

  public void addKill() {
    this.kills++;
  }

  public void addDeath() {
    this.deaths++;
  }

  public void addDestroyedBed() {
    this.destroyedBeds++;
  }
}
