package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public class RespawnMobCommand implements SubCommand {
  @Override
  public @NotNull String name() {
    return "respawnmob";
  }

  @Override
  public void execute(GamePlayer gamePlayer, String[] arguments) throws Exception {
    GameManager.instance().lobbyNpcManager().respawnAll();
    gamePlayer.sendMessage("Die NPCs wurden neu geladen.");
  }
}
