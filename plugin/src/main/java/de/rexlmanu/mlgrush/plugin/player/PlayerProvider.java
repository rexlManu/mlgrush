package de.rexlmanu.mlgrush.plugin.player;

import de.rexlmanu.mlgrush.plugin.game.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerProvider {

  public static final List<GamePlayer> PLAYERS = new ArrayList<>();

  /**
   * Find a {@link GamePlayer} by its unique id
   *
   * @param uniqueId uuid
   * @return a optional {@link GamePlayer}
   */
  public static Optional<GamePlayer> find(@NotNull UUID uniqueId) {
    return PLAYERS.stream().filter(gamePlayer -> gamePlayer.uniqueId().equals(uniqueId)).findAny();
  }

  public static List<GamePlayer> getPlayers(Environment environment) {
    return PLAYERS.stream().filter(gamePlayer -> gamePlayer.environment().equals(environment)).collect(Collectors.toList());
  }

}
