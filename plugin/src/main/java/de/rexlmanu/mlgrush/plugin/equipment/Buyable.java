package de.rexlmanu.mlgrush.plugin.equipment;

import org.bukkit.Material;

public interface Buyable {

  String name();

  default String permission() {
    return "mlgrush.equipment." + this.name().toLowerCase();
  }

  String displayName();

  int cost();

  Material material();

}
