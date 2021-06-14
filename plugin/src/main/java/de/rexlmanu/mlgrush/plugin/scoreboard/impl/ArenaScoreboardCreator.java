package de.rexlmanu.mlgrush.plugin.scoreboard.impl;

import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.scoreboard.ScoreboardCreator;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.RandomElement;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArenaScoreboardCreator implements ScoreboardCreator {
  @Override
  public void updateLines(GamePlayer gamePlayer) {
    GameManager.instance().arenaManager().arenaContainer().findArenaByPlayer(gamePlayer).ifPresent(arena -> {
      GameTeam team = arena.getTeam(gamePlayer);
      String enemy = arena.gameTeams()
        .stream()
        .filter(gameTeam -> !gameTeam.equals(team))
        .map(GameTeam::members)
        .map(RandomElement::of)
        .filter(Objects::nonNull)
        .map(target -> target.player().getName())
        .findFirst()
        .orElse("Unbekannt");
      gamePlayer.fastBoard().updateLines(Stream.of(
        "",
        "&8■ &7Dein Gegner",
        "&8 » &e" + enemy,
        "",
        "&8■ &7Deine Punkte",
        "&8 » &e" + team.points(),
        "",
        "&8■ &7Arena",
        "&8 » &e" + arena.configuration().arenaTemplate().name(),
        ""
      ).map(MessageFormat::replaceColors).collect(Collectors.toList()));
    });
  }

  @Override
  public void updateTablist(GamePlayer gamePlayer) {
    GameManager.instance().arenaManager().arenaContainer().findArenaByPlayer(gamePlayer).ifPresent(arena -> {
      Player player = gamePlayer.player();
      if (player == null) return;
      Scoreboard scoreboard = player.getScoreboard();
      scoreboard.getTeams().forEach(Team::unregister);
      arena.gameTeams().forEach(gameTeam -> {
        Team team = scoreboard.registerNewTeam(gameTeam.name().key());
        team.setPrefix(String.valueOf(gameTeam.name().color()));
        gameTeam.members().stream().map(GamePlayer::player).map(HumanEntity::getName).forEach(team::addEntry);
      });
      player.setScoreboard(scoreboard);
    });
  }
}
