package de.rexlmanu.mlgrush.plugin.arena.configuration;

import de.rexlmanu.mlgrush.arenalib.ArenaTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Location;

@AllArgsConstructor
@Data
@Accessors(fluent = true)
@Builder
public class ArenaConfiguration implements Cloneable {

  private int teamAmount, teamSize, maximalPoints, maximalGameLength, spawnProtection, buildHeight;
  private boolean autoBlockBreak, nohitdelay, knockbackOnlyHeight, custom;
  private ArenaTemplate arenaTemplate;

  private Location startPoint;

}
