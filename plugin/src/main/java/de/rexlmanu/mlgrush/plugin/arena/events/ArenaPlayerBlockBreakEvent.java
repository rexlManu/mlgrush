package de.rexlmanu.mlgrush.plugin.arena.events;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@Accessors(fluent = true)
@Getter
public class ArenaPlayerBlockBreakEvent extends PlayerEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  private GamePlayer gamePlayer;
  private Block block;

  public ArenaPlayerBlockBreakEvent(GamePlayer gamePlayer, Block block) {
    super(gamePlayer.player());
    this.gamePlayer = gamePlayer;
    this.block = block;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }
}
