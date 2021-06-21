package de.rexlmanu.mlgrush.plugin.game.npc;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.server.PluginDisableEvent;

@Accessors(fluent = true)
public class ArmorStandInteraction implements Listener {
  private String name;
  private InteractionHandler handler;

  public ArmorStandInteraction(String name, InteractionHandler handler) {
    this.name = name;
    this.handler = handler;

    Bukkit.getPluginManager().registerEvents(this, GamePlugin.getProvidingPlugin(GamePlugin.class));
  }

  public void remove() {
    HandlerList.unregisterAll(this);
  }

  @EventHandler(ignoreCancelled = false)
  public void handle(PlayerInteractAtEntityEvent event) {
    Entity entity = event.getRightClicked();
    if (!(entity instanceof ArmorStand
      || !entity.isCustomNameVisible())
      || !this.name.equals(entity.getCustomName())) return;

    PlayerProvider.find(event.getPlayer().getUniqueId())
      .ifPresent(gamePlayer -> this.handler.handle(gamePlayer));
  }

  @EventHandler
  public void handle(PluginDisableEvent event) {
    if (!event.getPlugin().getName().equals("MLGRush")) {
      return;
    }
    this.remove();
  }
}
