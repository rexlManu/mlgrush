package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.CommandParameter;
import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SetRankingCommand implements SubCommand {
  @Override
  public @NotNull String name() {
    return "setranking";
  }

  @Override
  public void execute(GamePlayer gamePlayer, String[] arguments) throws Exception {
    CommandParameter.require("ranking", 0, arguments);
    Integer ranking = CommandParameter.cast("ranking", 0, arguments, Integer.class);
    Block targetBlock = gamePlayer.player().getTargetBlock((Set<Material>) null, 10);
    if (targetBlock == null || !(targetBlock.getState() instanceof Sign)) {
      gamePlayer.sendMessage("Bitte schaue auf ein Schild.");
      return;
    }
    GameManager.instance().locationProvider().set("ranking-" + ranking, targetBlock.getLocation());
    gamePlayer.sendMessage(String.format("Du hast den Ranking Kopf für &e%s &7gesetzt.", ranking));
  }
}
