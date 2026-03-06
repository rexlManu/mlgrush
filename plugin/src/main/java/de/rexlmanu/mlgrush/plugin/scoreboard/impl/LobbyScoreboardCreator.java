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
    { "Twitter", "&b@rexlManu" },
    { "GitHub", "&7github.com/rexlManu" },
  };

  private int currentStats;
  private final BukkitTask task;

  public LobbyScoreboardCreator() {
    this.currentStats = 0;
    this.task = Bukkit.getScheduler().runTaskTimer(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 20 * 3L);
  }

  @Override
  public void run() {
    PlayerProvider.getPlayers(Environment.LOBBY).forEach(this::updateLines);

    this.currentStats++;
    if (this.currentStats >= 3) {
      this.currentStats = 0;
    }
  }

  @Override
  public void updateLines(GamePlayer gamePlayer) {
    GameManager.instance().databaseContext().getRanking(gamePlayer.uniqueId()).whenComplete((rank, throwable) -> {
      if (throwable != null) {
        rank = -1;
      }
      String statsName = null;
      String statsValue = null;
      switch (this.currentStats) {
        case 0 -> {
          statsName = "Kills";
          statsValue = String.valueOf(gamePlayer.data().statistics().kills());
        }
        case 1 -> {
          statsName = "Tode";
          statsValue = String.valueOf(gamePlayer.data().statistics().deaths());
        }
        case 2 -> {
          statsName = "Siege";
          statsValue = String.valueOf(gamePlayer.data().statistics().wins());
        }
        default -> {
        }
      }
      gamePlayer.fastBoard().updateLines(Stream.of(
        "",
        "&7Dein Ranking&8:",
        "&8 » &a" + (rank == -1 ? "?" : (rank + ". Platz")),
        "",
        "&7Deine " + statsName + "&8:",
        "&8 » &a" + statsValue,
        "",
        "&7Warteschlange&8:",
        "&8 » &a" + GameManager.instance().queueController().playerQueue().size() + " Spieler",
        "",
        "&7Spieler im Spiel&8:",
        "&8 » &a" + PlayerProvider.getPlayers(Environment.ARENA).size() + " Spieler",
        ""
      ).map(MessageFormat::replaceColors).collect(Collectors.toList()));
    });
  }

  @Override
  public void updateTablist(GamePlayer gamePlayer) {
    Player player = gamePlayer.player();
    if (player == null) {
      return;
    }
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    Team lobbyTeam = scoreboard.registerNewTeam("00-lobby");
    lobbyTeam.setPrefix(MessageFormat.replaceColors("&7"));
    Team arenaTeam = scoreboard.registerNewTeam("10-arena");
    arenaTeam.setPrefix(MessageFormat.replaceColors("&8"));

    PlayerProvider.PLAYERS.forEach(target -> {
      if (target.player() == null) {
        return;
      }
      if (target.environment().equals(Environment.ARENA)) {
        Optional<Arena> arena = GameManager.instance().arenaManager().arenaContainer().findArenaByPlayer(target);
        if (arena.isPresent() && arena.get().spectators().contains(gamePlayer)) {
          GameTeam gameTeam = arena.get().getTeam(target);
          Team scoreboardTeam = scoreboard.getTeam(gameTeam.name().key());
          if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(gameTeam.name().key());
            scoreboardTeam.setPrefix(String.valueOf(gameTeam.name().color()));
          }
          scoreboardTeam.addEntry(target.player().getName());
        } else {
          arenaTeam.addEntry(target.player().getName());
        }
      } else {
        lobbyTeam.addEntry(target.player().getName());
      }
    });

    player.setScoreboard(scoreboard);
  }
}
