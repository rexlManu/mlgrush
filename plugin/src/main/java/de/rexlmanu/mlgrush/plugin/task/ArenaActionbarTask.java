package de.rexlmanu.mlgrush.plugin.task;

import com.cryptomorin.xseries.messages.ActionBar;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.team.GameTeam;
import de.rexlmanu.mlgrush.plugin.detection.Detection;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import org.bukkit.Bukkit;

import java.util.stream.Stream;

public class ArenaActionbarTask implements Runnable {

  public ArenaActionbarTask() {
    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 1);
  }

  @Override
  public void run() {
    GameManager.instance().arenaManager().arenaContainer().activeArenas().forEach(arena -> {
      arena.players().forEach(gamePlayer -> GameManager.instance().scoreboardHandler().update(gamePlayer));
      Stream.concat(arena.players().stream(), arena.spectators().stream()).filter(gamePlayer -> gamePlayer.player() != null).forEach(gamePlayer -> {
        long seconds = (System.currentTimeMillis() - arena.gameStart()) / 1000;
        StringBuilder prefix = new StringBuilder();
        StringBuilder suffix = new StringBuilder();
        int halfTeamCount = arena.gameTeams().size() / 2;
        for (int i = 0; i < arena.gameTeams().size(); i++) {
          GameTeam team = arena.gameTeams().get(i);
          if (halfTeamCount < (i + 1)) {
            suffix.append(team.name().color()).append(team.points());
            if (gamePlayer.inspectionMode()) {
              suffix.append(" &8● &d").append(this.format(team.members().get(0).detection()));
            }
          } else {
            if (gamePlayer.inspectionMode()) {
              prefix.append("&d").append(this.format(team.members().get(0).detection())).append(" &8● ");
            }
            prefix.append(team.name().color()).append(team.points());
          }
        }
        String message = MessageFormat.replaceColors(
          String.format(
            "%s &8▶ &7%02d:%02d:%02d &8▶ %s",
            prefix,
            seconds / 3600, (seconds % 3600) / 60, seconds % 60,
            suffix
          )
        );
        ActionBar.sendActionBar(gamePlayer.player(), message);
      });
    });
  }

  private String format(Detection detection) {
    // averageSecondly, average,
    return new StringBuilder().append("&d")
      .append(detection.clicks())
      .append("&7c&8, &d")
      .append(String.format("%.1f", detection.clickAverageSecondly()))
      .append("&7cps&8, &d")
      .append(detection.places())
      .append("&7p&8, &d")
      .append(String.format("%.1f", detection.placeAverageSecondly()))
      .append("&7pps&8, &d")
      .append(String.format("%.1f", detection.clickAverage()))
      .append("&7acps&8, &d")
      .append(String.format("%.1f", detection.placeAverage()))
      .append("&7apps&8, &d")
      .append(detection.transactionPing())
      .append("&7ms")
      .toString();
//      .append(detection.standardDeviation())
//      .append("&7sd").toString();
  }
}
