package de.rexlmanu.mlgrush.plugin.stats;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;
import de.rexlmanu.mlgrush.plugin.utility.UUIDFetcher;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

import java.util.UUID;

public class StatsHandler {

  public static void updateStatsWall() {
    GameManager.instance().databaseContext().getRankings(5).whenComplete((gamePlayerData, throwable) -> {
      if (throwable != null) {
        GamePlugin.getProvidingPlugin(GamePlugin.class).getLogger().severe("Could not update stats wall");
        return;
      }

      for (int i = 0; i < 5; i++) {
        int rank = i + 1;
        int finalI = i;
        GameManager.instance().locationProvider().get("ranking-" + rank).ifPresent(location -> {
          GamePlayerData data = new GamePlayerData(UUID.randomUUID());
          String owner = "MHF_QUESTION";
          String name = "???";
          if (gamePlayerData.size() > finalI) {
            data = gamePlayerData.get(finalI);
            name = UUIDFetcher.getName(UUID.fromString(data.getKey()));
            owner = name;
          }
          Sign sign = (Sign) location.getBlock().getState();
          sign.setLine(0, "---#" + rank + "---");
          sign.setLine(1, name);
          sign.setLine(2, data.statistics().kills() + " Kills");
          sign.setLine(3, data.statistics().wins() + " Wins");
          sign.update();
          Skull skull = (Skull) sign.getLocation().add(0, 1, 0).getBlock().getState();
          skull.setOwner(owner);
          skull.update();
        });
      }
    });
  }

}
