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
import net.pluginstube.api.CloudBasicFactory;
import net.pluginstube.api.scoreboard.ScoreboardTeamFactory;
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
    { "Twitter", "&b@PluginStubeNW" },
    { "TeamSpeak", "&3PluginStube.net" },
  };

  private int currentStats;
  private BukkitTask task;

  public LobbyScoreboardCreator() {
    this.currentStats = 0;
    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 20 * 3);
  }

  @Override
  public void run() {
    PlayerProvider.getPlayers(Environment.LOBBY).forEach(this::updateLines);

    this.currentStats++;
    if (this.currentStats >= 3) this.currentStats = 0;
  }

  @Override
  public void updateLines(GamePlayer gamePlayer) {
    GameManager.instance().databaseContext().getRanking(gamePlayer.uniqueId()).whenComplete((rank, throwable) -> {
      if (throwable != null) {
        rank = -1;
      }
      String statsName = null, statsValue = null;
      switch (this.currentStats) {
        case 0:
          statsName = "Kills";
          statsValue = String.valueOf(gamePlayer.data().statistics().kills());
          break;
        case 1:
          statsName = "Tode";
          statsValue = String.valueOf(gamePlayer.data().statistics().deaths());
          break;
        case 2:
          statsName = "Siege";
          statsValue = String.valueOf(gamePlayer.data().statistics().wins());
          break;
      }
      gamePlayer.fastBoard().updateLines(Stream.of(
        "",
        "&7Dein Ranking&8:",
        "&8 » &a" + rank + ". Platz",
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
    if (player == null) return;
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    ScoreboardTeamFactory factory = new ScoreboardTeamFactory();
    factory.registerAll();
    factory.buildAll(scoreboard);

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
        scoreboard.getTeam(factory.getTeamEntryByPermissionGroup(CloudBasicFactory.getBlankRank(target.uniqueId())))
          .addEntry(target.player().getName());
      }
    });

    player.setScoreboard(scoreboard);
  }
}
