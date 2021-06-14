package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.stats.StatsHandler;
import org.jetbrains.annotations.NotNull;

public class UpdateStatsWallCommand implements SubCommand {
  @Override
  public @NotNull String name() {
    return "updatestatswall";
  }

  @Override
  public void execute(GamePlayer gamePlayer, String[] arguments) throws Exception {
    gamePlayer.sendMessage("Die Stats Wall wird nun aktualisiert.");
    StatsHandler.updateStatsWall();
  }
}
