package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public class NickCommand implements SubCommand {
  @Override
  public @NotNull String name() {
    return "nick";
  }

  @Override
  public void execute(GamePlayer gamePlayer, String[] arguments) {
    String nickname = arguments.length > 0 ? arguments[0] : "MioNaruse";
    GameManager.instance().nicknameService().register(gamePlayer.player(), nickname);
    gamePlayer.sendMessage("Du wurdest genickt.");
    GameManager.instance().scoreboardHandler().updateAll(gamePlayer.environment());
  }
}
