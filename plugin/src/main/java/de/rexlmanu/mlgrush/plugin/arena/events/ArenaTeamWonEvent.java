package de.rexlmanu.mlgrush.plugin.arena.events;

import de.rexlmanu.mlgrush.plugin.arena.Arena;
import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Accessors(fluent = true)
@Getter
public class ArenaTeamWonEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  private Arena arena;
  private GameTeam gameTeam;

  public ArenaTeamWonEvent(Arena arena, GameTeam gameTeam) {
    this.arena = arena;
    this.gameTeam = gameTeam;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }
}
