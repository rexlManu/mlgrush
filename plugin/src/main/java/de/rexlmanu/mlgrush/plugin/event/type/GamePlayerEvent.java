package de.rexlmanu.mlgrush.plugin.event.type;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class GamePlayerEvent<E> extends GameEvent<E> {
  private GamePlayer gamePlayer;

  public GamePlayerEvent(E event, GamePlayer gamePlayer) {
    super(event);
    this.gamePlayer = gamePlayer;
  }
}
