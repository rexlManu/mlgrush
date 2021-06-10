package de.rexlmanu.mlgrush.plugin.scoreboard;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;

public interface ScoreboardCreator {

  void updateLines(GamePlayer gamePlayer);

  void updateTablist(GamePlayer gamePlayer);

}
