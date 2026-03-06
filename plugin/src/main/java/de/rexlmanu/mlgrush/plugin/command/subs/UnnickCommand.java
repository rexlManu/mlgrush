package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public class UnnickCommand implements SubCommand {
  @Override
  public @NotNull String name() {
    return "unnick";
  }

  @Override
  public void execute(GamePlayer gamePlayer, String[] arguments) {
    GameManager.instance().nicknameService().unregister(gamePlayer.player());
    gamePlayer.sendMessage("Du wurdest entnickt.");
    GameManager.instance().scoreboardHandler().updateAll(gamePlayer.environment());
  }
}
