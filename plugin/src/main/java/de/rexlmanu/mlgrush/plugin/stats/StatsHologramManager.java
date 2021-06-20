package de.rexlmanu.mlgrush.plugin.stats;

import de.rexlmanu.mlgrush.plugin.command.StatsCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.Statistics;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.hologram.VirtualHologram;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatsHologramManager {
  private Map<UUID, VirtualHologram> hologramMap;
  private Location location;

  public StatsHologramManager() {
    this.hologramMap = new HashMap<>();
    this.location = GameManager.instance().locationProvider().get("hologram").orElse(null);
    if (this.location != null) this.location.add(0, 0.5, 0);
  }

  private CompletableFuture<VirtualHologram> create(GamePlayer gamePlayer) {
    Statistics statistics = gamePlayer.data().statistics();
    return GameManager.instance().databaseContext().getRanking(gamePlayer.uniqueId()).thenApply((rank) -> new VirtualHologram(this.location, Stream.of(
      "",
      String.format("&8▶ &7Ranking &8● &a%s", rank),
      "",
      String.format("&8▶ &7Kills &8● &a%s", statistics.kills()),
      String.format("&8▶ &7Tode &8● &a%s", statistics.deaths()),
      String.format("&8▶ &7KD &8● &a%.2f", StatsCommand.checkForNan(statistics.kills(), statistics.deaths())),
      "",
      String.format("&8▶ &7Abgebaute Betten &8● &a%s", statistics.destroyedBeds()),
      String.format("&8▶ &7Platzierte Blöcke &8● &a%s", statistics.blocks()),
      String.format("&8▶ &7Siegreiche Spiele &8● &a%s", statistics.wins()),
      String.format("&8▶ &7Gespielte Spiele &8● &a%s", statistics.games()),
      "",
      String.format("&8▶ &7Siegesrate &8● &a%.1f%%", StatsCommand.checkForNan(statistics.wins(), statistics.games()) * 100),
      ""
    ).map(MessageFormat::replaceColors).collect(Collectors.toList())));
  }

  public void show(GamePlayer gamePlayer) {
    if (this.location == null || gamePlayer.data() == null)
      return;
    this.create(gamePlayer).thenAccept(virtualHologram -> {
      if (this.hologramMap.containsKey(gamePlayer.uniqueId())) {
        this.hologramMap.get(gamePlayer.uniqueId()).destroy(gamePlayer.player());
      }
      this.hologramMap.put(gamePlayer.uniqueId(), virtualHologram);
      virtualHologram.send(gamePlayer.player());
    });
  }

  public void destroy(GamePlayer gamePlayer) {
    VirtualHologram virtualHologram = this.hologramMap.get(gamePlayer.uniqueId());
    if (virtualHologram != null)
      virtualHologram.destroy(gamePlayer.player());
    this.hologramMap.remove(gamePlayer.uniqueId());
  }
}
