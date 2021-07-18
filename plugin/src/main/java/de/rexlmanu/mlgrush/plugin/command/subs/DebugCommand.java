package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.arena.Arena;
import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.task.particle.RotatingParticleTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class DebugCommand implements SubCommand {
  @Override
  public @NotNull String name() {
    return "debug";
  }

  @Override
  public void execute(GamePlayer gamePlayer, String[] arguments) throws Exception {
    gamePlayer.sendMessage("Aktive Arenas:");
    for (Arena activeArena : GameManager.instance().arenaManager().arenaContainer().activeArenas()) {
      gamePlayer.sendMessage(String.format("- %s - %s s",
        activeArena.players().stream().map(GamePlayer::player).map(Player::getName).collect(Collectors.joining()),
        (System.currentTimeMillis() - activeArena.gameStart()) / 1000
      ));
    }


    gamePlayer.sendMessage("creating circle");
    new RotatingParticleTask();
  }
}
