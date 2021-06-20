package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import org.jetbrains.annotations.NotNull;

public class InspectionCommand implements SubCommand {
  @Override
  public @NotNull String name() {
    return "inspection";
  }

  @Override
  public @NotNull String description() {
    return "Schalte den InspectionMode um.";
  }

  @Override
  public void execute(GamePlayer gamePlayer, String[] arguments) throws Exception {
    gamePlayer.inspectionMode(!gamePlayer.inspectionMode());
    gamePlayer.sendMessage(String.format("Du hast den &dInspectionMode&7 %s.", gamePlayer.inspectionMode() ? "aktiviert" : "deaktiviert"));
  }
}
