package de.rexlmanu.mlgrush.plugin.arena.events;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true)
@Getter
public class ArenaPlayerDiedEvent extends PlayerEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  private GamePlayer gamePlayer;
  @Nullable
  private GamePlayer killer;

  public ArenaPlayerDiedEvent(GamePlayer gamePlayer, GamePlayer killer) {
    super(gamePlayer.player());
    this.gamePlayer = gamePlayer;
    this.killer = gamePlayer.equals(killer) ? null : killer;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }
}
