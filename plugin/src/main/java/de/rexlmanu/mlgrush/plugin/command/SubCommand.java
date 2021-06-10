package de.rexlmanu.mlgrush.plugin.command;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface SubCommand {

  @NotNull
  String name();

  @NotNull
  default String description() {
    return "";
  }

  void execute(GamePlayer gamePlayer, String[] arguments) throws Exception;

  @NotNull
  default List<String> suggestions(GamePlayer gamePlayer, String[] arguments) {
    return new ArrayList<>();
  }
}
