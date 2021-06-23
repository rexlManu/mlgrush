package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.stats.StatsHandler;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public class ForceStatsWallCommand implements SubCommand {
  @Override
  public @NotNull String name() {
    return "forcestatswall";
  }

  @Override
  public void execute(GamePlayer gamePlayer, String[] arguments) throws Exception {
    StatsHandler.updateStatsWall();
    gamePlayer.sendMessage("Stats Wall wird geupdatet.");
  }
}
