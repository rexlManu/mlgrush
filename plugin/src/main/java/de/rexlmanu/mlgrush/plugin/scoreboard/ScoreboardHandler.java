package de.rexlmanu.mlgrush.plugin.scoreboard;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.scoreboard.impl.ArenaScoreboardCreator;
import de.rexlmanu.mlgrush.plugin.scoreboard.impl.LobbyScoreboardCreator;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardHandler {

  private Map<Environment, ScoreboardCreator> environmentScoreboardCreatorMap;

  public ScoreboardHandler() {
    this.environmentScoreboardCreatorMap = new HashMap<>();
    this.environmentScoreboardCreatorMap.put(Environment.LOBBY, new LobbyScoreboardCreator());
    this.environmentScoreboardCreatorMap.put(Environment.ARENA, new ArenaScoreboardCreator());
  }

  public void update(GamePlayer gamePlayer) {
    ScoreboardCreator creator = this.environmentScoreboardCreatorMap.get(gamePlayer.environment());
    creator.updateLines(gamePlayer);
    Bukkit.getScheduler().runTask(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> {
      creator.updateTablist(gamePlayer);
    });
  }

  public void updateAll(Environment environment) {
    PlayerProvider.getPlayers(environment).forEach(this::update);
  }

}
