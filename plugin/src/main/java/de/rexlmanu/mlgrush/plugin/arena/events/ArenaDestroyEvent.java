package de.rexlmanu.mlgrush.plugin.arena.events;

import de.rexlmanu.mlgrush.plugin.arena.Arena;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Accessors(fluent = true)
@Getter
public class ArenaDestroyEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  private Arena arena;

  public ArenaDestroyEvent(Arena arena) {
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
