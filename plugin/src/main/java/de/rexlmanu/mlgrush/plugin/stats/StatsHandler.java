package de.rexlmanu.mlgrush.plugin.stats;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;
import de.rexlmanu.mlgrush.plugin.utility.UUIDFetcher;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatsHandler {

  public static int getRanking(GamePlayer gamePlayer) {
    List<GamePlayerData> playerDataList = GamePlugin.getPlugin(GamePlugin.class).repository().all().stream().sorted((o1, o2) -> o2.statistics().wins() - o1.statistics().wins()).collect(Collectors.toList());
    GamePlayerData data = playerDataList.stream().filter(gamePlayerData -> gamePlayerData.getKey().equals(gamePlayer.uniqueId().toString())).findAny().orElse(null);
    if (data == null) return -1;
    return playerDataList.indexOf(data) + 1;
  }

  public static void updateStatsWall() {
    for (int i = 0; i < 5; i++) {
      GamePlayerData playerDataByRank = getPlayerDataByRank(i);
      if (playerDataByRank == null) break;
      int rank = i + 1;
      GameManager.instance().locationProvider().get("ranking-" + rank).ifPresent(location -> {
        String name = UUIDFetcher.getName(UUID.fromString(playerDataByRank.getKey()));
        Sign sign = (Sign) location.getBlock().getState();
        sign.setLine(0, "---#" + rank + "---");
        sign.setLine(1, name);
        sign.setLine(2, playerDataByRank.statistics().kills() + " Kills");
        sign.setLine(3, playerDataByRank.statistics().wins() + " Wins");
        sign.update();
        Skull skull = (Skull) sign.getLocation().add(0, 1, 0).getBlock().getState();
        skull.setOwner(name);
        skull.update();
      });
    }
  }

  public static GamePlayerData getPlayerDataByRank(int rank) {
    List<GamePlayerData> data = GamePlugin
      .getPlugin(GamePlugin.class)
      .repository()
      .all()
      .stream()
      .sorted((o1, o2) -> o2.statistics().wins() - o1.statistics().wins())
      .collect(Collectors.toList());
    return data.size() <= rank ? null : data.get(rank);
  }

}
