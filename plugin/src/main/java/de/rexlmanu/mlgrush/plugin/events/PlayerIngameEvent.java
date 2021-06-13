package de.rexlmanu.mlgrush.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerIngameEvent extends PlayerEvent {
  private static final HandlerList HANDLER_LIST = new HandlerList();

  public PlayerIngameEvent(Player who) {
    super(who);
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }
}
