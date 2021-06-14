package de.rexlmanu.mlgrush.plugin.stats.npc;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import net.jitse.npclib.NPCLib;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class StatsNPCProvider implements Runnable {

  private NPCLib npcLibrary;
  private List<StatsNPC> statsNPCS;

  public StatsNPCProvider() {
    this.npcLibrary = new NPCLib(GamePlugin.getProvidingPlugin(GamePlugin.class));
    this.statsNPCS = new ArrayList<>();
    GameManager.instance().databaseContext().getRankings(3).whenComplete((gamePlayerData, throwable) -> {
      for (int i = 0; i < gamePlayerData.size(); i++) {
        int finalI = i;
        GameManager.instance().locationProvider().get("npc-ranking-" + (i + 1)).ifPresent(location -> {
          this.statsNPCS.add(new StatsNPC(npcLibrary, gamePlayerData.get(finalI), finalI + 1, location));
        });
      }
    });
    Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 1);
  }

  @Override
  public void run() {
    Bukkit.getOnlinePlayers().forEach(player -> {
      this.statsNPCS.forEach(statsNPC -> {
        if (!player.getLocation().getWorld().equals(statsNPC.getLocation().getWorld()) || player.getLocation().distance(statsNPC.getLocation()) > 100) {
          if (statsNPC.getNpc().isShown(player)) {
            statsNPC.getNpc().hide(player);
          }
          return;
        }
        if (!statsNPC.getNpc().isShown(player)) {
          statsNPC.getNpc().show(player);
        }
        if (player.getLocation().distance(statsNPC.getLocation()) < 5) {
          //statsNPC.getNpc().lookAt(player.getLocation());
        }
      });
    });
  }
}
