package de.rexlmanu.mlgrush.plugin.command.subs;

import de.rexlmanu.mlgrush.plugin.command.CommandParameter;
import de.rexlmanu.mlgrush.plugin.command.SubCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

  @Override
  public @NotNull List<String> suggestions(GamePlayer gamePlayer, String[] arguments) {
    if (arguments.length == 1) {
      return IntStream.range(1, 5).mapToObj(String::valueOf).collect(Collectors.toList());
    }
    return new ArrayList<>();
  }
}
