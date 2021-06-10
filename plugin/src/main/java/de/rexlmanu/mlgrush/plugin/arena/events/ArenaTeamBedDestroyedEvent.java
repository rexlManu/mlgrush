package de.rexlmanu.mlgrush.plugin.arena.events;

import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@Accessors(fluent = true)
@Getter
public class ArenaTeamBedDestroyedEvent extends PlayerEvent {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  private GamePlayer gamePlayer;
  private GameTeam team;

  public ArenaTeamBedDestroyedEvent(GamePlayer gamePlayer, GameTeam team) {
    super(gamePlayer.player());
    this.gamePlayer = gamePlayer;
    this.team = team;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }
}
