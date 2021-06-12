package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

public class BuildCommand implements SubCommand {
  @Override
  public @NotNull String name() {
    return "build";
  }

  @Override
  public @NotNull String description() {
    return "Schalte den BuildMode um.";
  }

  @Override
  public void execute(GamePlayer gamePlayer, String[] arguments) throws Exception {
    gamePlayer.buildMode(!gamePlayer.buildMode());
    gamePlayer.sendMessage(String.format("Du hast den &eBuildMode&7 %s.", gamePlayer.buildMode() ? "aktiviert" : "deaktiviert"));
    gamePlayer.player().setGameMode(gamePlayer.buildMode() ? GameMode.CREATIVE : GameMode.ADVENTURE);
  }
}
