package de.rexlmanu.mlgrush.plugin.arena.team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.ChatColor;

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
public enum TeamColor {
  RED("Rot", ChatColor.RED),
  BLUE("Blau", ChatColor.BLUE);

  private String colorName;
  private ChatColor color;

  public String key() {
    return this.name().toLowerCase();
  }

  public String displayName() {
    return this.color + this.colorName;
  }
}
