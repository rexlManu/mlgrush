package de.rexlmanu.mlgrush.plugin.stats.npc;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;
import de.rexlmanu.mlgrush.plugin.utility.UUIDFetcher;
import de.rexlmanu.mlgrush.plugin.utility.fetcher.SkinFetcher;
import lombok.Data;
import lombok.experimental.Accessors;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import java.util.Arrays;

@Accessors
@Data
public class StatsNPC implements Listener {

  private NPCLib npcLibrary;
  private GamePlayerData gamePlayerData;
  private int rank;
  private Location location;

  private NPC npc;

  public StatsNPC(NPCLib npcLibrary, GamePlayerData gamePlayerData, int rank, Location location) {
    this.npcLibrary = npcLibrary;
    this.gamePlayerData = gamePlayerData;
    this.rank = rank;
    this.location = location;
    Bukkit.getPluginManager().registerEvents(this, GamePlugin.getProvidingPlugin(GamePlugin.class));

    this.create();
  }

  private void create() {
    String name = UUIDFetcher.getName(this.gamePlayerData.uniqueId());
    this.npc = this.npcLibrary.createNPC(Arrays.asList(
      name
    ));
    SkinFetcher.fetch(gamePlayerData.uniqueId()).whenComplete((skin, throwable) -> {
      if (throwable == null) return;
      this.npc.setSkin(skin);
      this.npc.setLocation(this.location);
      this.npc.create();
    });
  }

}
