package de.rexlmanu.mlgrush.plugin.integration.pluginstube;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.scoreboard.impl.LobbyScoreboardCreator;
import net.pluginstube.library.CloudBasicFactory;
import net.pluginstube.library.scoreboard.ScoreboardTeamFactory;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;

public class PluginStubeLobbyScoreboard extends LobbyScoreboardCreator {

  private ScoreboardTeamFactory factory;

  public PluginStubeLobbyScoreboard() {
    super();
    this.factory = new ScoreboardTeamFactory();
    this.factory.registerAll();
  }

  @Override
  public void updateLines(GamePlayer gamePlayer) {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    factory.buildAll(scoreboard);
    PlayerProvider.PLAYERS.forEach(target -> {
      String entry = this.factory.getTeamEntryByPermissionGroup(CloudBasicFactory.getBlankRank(target.uniqueId()));
      scoreboard.getTeam(entry).addEntry(target.player().getName());
    });
    gamePlayer.player().setScoreboard(scoreboard);
  }
}
