package de.rexlmanu.mlgrush.plugin.arena.events;

import de.rexlmanu.mlgrush.plugin.arena.Arena;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@Accessors(fluent = true)
@Getter
public class ArenaPlayerLeftEvent extends PlayerEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  private GamePlayer gamePlayer;
  private Arena arena;

  public ArenaPlayerLeftEvent(GamePlayer gamePlayer, Arena arena) {
    super(gamePlayer.player());
    this.gamePlayer = gamePlayer;
    this.arena = arena;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }
}
