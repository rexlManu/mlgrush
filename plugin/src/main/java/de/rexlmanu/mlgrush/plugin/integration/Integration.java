package de.rexlmanu.mlgrush.plugin.integration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public enum Integration {
//  PLUGINSTUBE(Bukkit.getPluginManager().isPluginEnabled("library-spigot-1.0"), PluginStubeIntegration.class);
  ;

  private boolean available;
  private Class<? extends GameIntegration> gameIntegration;
}
