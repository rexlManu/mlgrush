package de.rexlmanu.mlgrush.plugin.scoreboard.impl;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.scoreboard.ScoreboardCreator;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.RandomElement;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Accessors(fluent = true)
public class ArenaScoreboardCreator implements ScoreboardCreator, Runnable {
  private int currentAd;
  private BukkitTask task;

  public ArenaScoreboardCreator() {
    this.currentAd = 0;
    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 20 * 3);
  }

  @Override
  public void run() {
    PlayerProvider.getPlayers(Environment.ARENA).forEach(this::updateLines);

    this.currentAd++;
    if (this.currentAd >= LobbyScoreboardCreator.ADS.length) this.currentAd = 0;
  }

  @Override
  public void updateLines(GamePlayer gamePlayer) {
    String[] ad = LobbyScoreboardCreator.ADS[this.currentAd];
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
        "",
        "&8■ &7" + ad[0],
        "&8 » &e" + ad[1],
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
