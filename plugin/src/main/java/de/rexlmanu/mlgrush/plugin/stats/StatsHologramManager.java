package de.rexlmanu.mlgrush.plugin.stats;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.command.StatsCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.Statistics;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.hologram.VirtualHologram;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class StatsHologramManager {
  private Map<UUID, VirtualHologram> hologramMap;
  private Location location;

  public StatsHologramManager() {
    this.hologramMap = new HashMap<>();
    this.location = GameManager.instance().locationProvider().get("hologram").orElse(null);
    if (this.location != null) this.location.add(0, 2.8, 0);
  }

  private CompletableFuture<VirtualHologram> create(GamePlayer gamePlayer) {
    Statistics statistics = gamePlayer.data().statistics();
    return GameManager.instance()
        .databaseContext()
        .getRanking(gamePlayer.uniqueId())
        .thenApply(
            (rank) -> {
              VirtualHologram hologram =
                  new VirtualHologram(
                      this.location,
                      Stream.of(
                              "",
                              String.format("&8▶ &7Ranking &8● &a%s", rank == -1 ? "?" : rank),
                              "",
                              String.format("&8▶ &7Kills &8● &a%s", statistics.kills()),
                              String.format("&8▶ &7Tode &8● &a%s", statistics.deaths()),
                              String.format(
                                  "&8▶ &7KD &8● &a%.2f",
                                  StatsCommand.checkForNan(
                                      statistics.kills(), statistics.deaths())),
                              "",
                              String.format(
                                  "&8▶ &7Abgebaute Betten &8● &a%s", statistics.destroyedBeds()),
                              String.format("&8▶ &7Siegreiche Spiele &8● &a%s", statistics.wins()),
                              String.format("&8▶ &7Gespielte Spiele &8● &a%s", statistics.games()),
                              "",
                              String.format(
                                  "&8▶ &7Siegesrate &8● &a%.1f%%",
                                  StatsCommand.checkForNan(statistics.wins(), statistics.games())
                                      * 100),
                              "")
                          .map(MessageFormat::replaceColors)
                          .collect(Collectors.toList()));
              hologram.setDistance_above(-0.3D);
              return hologram;
            });
  }

  public void show(GamePlayer gamePlayer) {
    if (this.location == null || gamePlayer.data() == null) return;
    this.create(gamePlayer)
        .thenAccept(
            virtualHologram -> {
              Bukkit.getScheduler()
                  .runTask(
                      GamePlugin.getProvidingPlugin(GamePlugin.class),
                      () -> {
                        if (this.hologramMap.containsKey(gamePlayer.uniqueId())) {
                          this.hologramMap.get(gamePlayer.uniqueId()).destroy(gamePlayer.player());
                        }
                        this.hologramMap.put(gamePlayer.uniqueId(), virtualHologram);
                        virtualHologram.send(gamePlayer.player());
                      });
            });
  }

  public void destroy(GamePlayer gamePlayer) {
    VirtualHologram virtualHologram = this.hologramMap.get(gamePlayer.uniqueId());
    if (virtualHologram != null) virtualHologram.destroy(gamePlayer.player());
    this.hologramMap.remove(gamePlayer.uniqueId());
  }
}
