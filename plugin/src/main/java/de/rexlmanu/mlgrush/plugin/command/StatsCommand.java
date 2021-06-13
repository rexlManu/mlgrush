package de.rexlmanu.mlgrush.plugin.command;

import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.player.Statistics;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class StatsCommand implements CommandExecutor {
  private Map<UUID, Long> cooldown;

  public StatsCommand() {
    this.cooldown = new HashMap<>();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) return true;
    PlayerProvider.find(((Player) sender).getUniqueId()).ifPresent(gamePlayer -> {
      if (args.length == 1) {
        String name = args[0];
        if (name.length() > 16 || name.length() < 3) {
          gamePlayer.sendMessage("Dieser Spieler konnte nicht gefunden werden.");
          return;
        }
        if (Bukkit.getPlayer(name) != null) {
          PlayerProvider.find(Bukkit.getPlayer(name).getUniqueId()).ifPresent(target -> this.printStats(gamePlayer, target.data(), target.player().getName()));
          return;
        }
        if (this.cooldown.containsKey(gamePlayer.uniqueId()) && !UUIDFetcher.isCached(name)) {
          gamePlayer.sendMessage("Bitte gedulde dich noch bis du wieder Stats abfragen darfst.");
          return;
        }
        UUIDFetcher.getUUID(name, uuid -> GameManager.instance().databaseContext().loadData(uuid).whenComplete((gamePlayerData, throwable) -> {
          this.cooldown.put(gamePlayer.uniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(90));
          if (throwable != null) {
            gamePlayer.sendMessage("Dieser Spieler konnte nicht gefunden werden.");
            return;
          }
          this.printStats(gamePlayer, gamePlayerData, name);
        }));
        return;
      }
      this.printStats(gamePlayer, gamePlayer.data(), gamePlayer.player().getName());
    });

    return true;
  }

  private void printStats(GamePlayer gamePlayer, GamePlayerData data, String name) {
    GameManager.instance().databaseContext().getRanking(data.uniqueId()).whenComplete((rank, throwable) -> {
      if (throwable != null) {
        rank = -1;
      }
      Player player = gamePlayer.player();
      player.sendMessage("");
      if (player.getName().equalsIgnoreCase(name)) {
        gamePlayer.sendMessage("Deine Statistiken");
      } else {
        gamePlayer.sendMessage(String.format("Die Stats von &e%s", name));
      }
      Statistics statistics = data.statistics();
      Stream.of(
        "",
        String.format("&e  × &7Ranking &8» &e%s", rank),
        "",
        String.format("&e  × &7Kills &8» &e%s", statistics.kills()),
        String.format("&e  × &7Tode &8» &e%s", statistics.deaths()),
        String.format("&e  × &7KD &8» &e%.2f", checkForNan(statistics.wins(), statistics.games()) * 100),
        "",
        String.format("&e  × &7Platzierte Blöcke &8» &e%s", statistics.blocks()),
        String.format("&e  × &7Siegreiche Spiele &8» &e%s", statistics.wins()),
        String.format("&e  × &7Gespielte Spiele &8» &e%s", statistics.games()),
        "",
        String.format("&e  × &7Siegesrate &8» &e%.2f%%", checkForNan(statistics.wins(), statistics.games()) * 100),
        ""
      ).map(MessageFormat::replaceColors).forEach(player::sendMessage);
    });

  }

  private double checkForNan(double divider, double value) {
    if (divider == 0) {
      if (value != 0) return value;
      return 0;
    }
    return divider / value;
  }
}
