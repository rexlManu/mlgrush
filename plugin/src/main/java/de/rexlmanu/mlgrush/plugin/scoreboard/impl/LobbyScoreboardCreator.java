package de.rexlmanu.mlgrush.plugin.scoreboard.impl;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.Arena;
import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.scoreboard.ScoreboardCreator;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Accessors(fluent = true)
public class LobbyScoreboardCreator implements ScoreboardCreator, Runnable {

  public static final String[][] ADS = {
    { "Twitter", "@rexlManu" },
    { "Discord", "rexlManu#1337" },
    { "Github", "rexlManu" },
  };

  private int currentAd;
  private BukkitTask task;

  public LobbyScoreboardCreator() {
    this.currentAd = 0;
    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 20 * 3);
  }

  @Override
  public void run() {
    PlayerProvider.getPlayers(Environment.LOBBY).forEach(this::updateLines);

    this.currentAd++;
    if (this.currentAd >= ADS.length) this.currentAd = 0;
  }

  @Override
  public void updateLines(GamePlayer gamePlayer) {
    String[] ad = ADS[this.currentAd];
    GameManager.instance().databaseContext().getRanking(gamePlayer.uniqueId()).whenComplete((rank, throwable) -> {
      if (throwable != null) {
        rank = -1;
      }
      gamePlayer.fastBoard().updateLines(Stream.of(
        "",
        "&8■ &7Dein Ranking",
        "&8 » &e" + rank + ". Platz",
        "",
        "&8■ &7Warteschlange",
        "&8 » &e" + GameManager.instance().queueController().playerQueue().size() + " Spieler",
        "",
        "&8■ &7Spieler im Spiel",
        "&8 » &e" + PlayerProvider.getPlayers(Environment.ARENA).size() + " Spieler",
        "",
        "&8■ &7" + ad[0],
        "&8 » &e" + ad[1],
        ""
      ).map(MessageFormat::replaceColors).collect(Collectors.toList()));
    });
  }

  @Override
  public void updateTablist(GamePlayer gamePlayer) {
    Player player = gamePlayer.player();
    if (player == null) return;
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    Team teamLobby = scoreboard.registerNewTeam("1-lobby");
    teamLobby.setPrefix(MessageFormat.replaceColors("&7"));
    Team teamIngame = scoreboard.registerNewTeam("2-ingame");
    teamIngame.setPrefix(MessageFormat.replaceColors("&8"));
    PlayerProvider.PLAYERS.forEach(target -> {
      if (target.environment().equals(Environment.ARENA)) {
        Optional<Arena> any = GameManager.instance().arenaManager().arenaContainer().findArenaByPlayer(target);
        Arena arena = any.get();
        if (arena.spectators().contains(gamePlayer)) {
          GameTeam team = arena.getTeam(target);
          Team scoreboardTeam = scoreboard.getTeam(team.name().key());
          if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(team.name().key());
            scoreboardTeam.setPrefix(String.valueOf(team.name().color()));
          }

          scoreboardTeam.addEntry(target.player().getName());
        } else {
          teamIngame.addEntry(target.player().getName());
        }
      } else {
        teamLobby.addEntry(target.player().getName());
      }
    });

    player.setScoreboard(scoreboard);
  }
}
