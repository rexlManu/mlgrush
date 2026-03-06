package de.rexlmanu.mlgrush.plugin.stats;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;
import de.rexlmanu.mlgrush.plugin.utility.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatsHandler {

  public static void updateStatsWall() {
    GameManager.instance().databaseContext().getRankings(5).whenComplete((gamePlayerData, throwable) -> {
      JavaPlugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
      if (throwable != null) {
        plugin.getLogger().severe("Could not update stats wall");
        return;
      }

      List<StatsWallEntry> entries = new ArrayList<>();
      for (int i = 0; i < 5; i++) {
        int rank = i + 1;
        if (gamePlayerData.size() <= i) {
          entries.add(new StatsWallEntry(rank, "???", 0, 0, null));
          continue;
        }

        GamePlayerData data = gamePlayerData.get(i);
        UUID uniqueId = UUID.fromString(data.getKey());
        String name = UUIDFetcher.getName(uniqueId);
        if (name == null || name.isBlank()) {
          name = "???";
        }
        entries.add(new StatsWallEntry(rank, name, data.statistics().kills(), data.statistics().wins(), uniqueId));
      }

      Bukkit.getScheduler().runTask(plugin, () -> {
        entries.forEach(entry -> {
          GameManager.instance().locationProvider().get("ranking-" + entry.rank()).ifPresent(location -> {
            if (!(location.getBlock().getState() instanceof Sign sign)) {
              return;
            }

            sign.setLine(0, "---#" + entry.rank() + "---");
            sign.setLine(1, entry.name());
            sign.setLine(2, entry.kills() + " Kills");
            sign.setLine(3, entry.wins() + " Wins");
            sign.update();

            if (sign.getLocation().add(0, 1, 0).getBlock().getState() instanceof Skull skull) {
              if (entry.uniqueId() == null) {
                skull.setOwner("MHF_QUESTION");
              } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.uniqueId());
                skull.setOwningPlayer(offlinePlayer);
              }
              skull.update();
            }
          });
        });
      });
    });
  }

  private record StatsWallEntry(int rank, String name, int kills, int wins, UUID uniqueId) {
  }

}
