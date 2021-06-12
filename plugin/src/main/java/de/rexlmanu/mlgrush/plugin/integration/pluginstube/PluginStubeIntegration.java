package de.rexlmanu.mlgrush.plugin.integration.pluginstube;

import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.StickEquipment;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.integration.GameIntegration;
import de.rexlmanu.mlgrush.plugin.scoreboard.ScoreboardCreator;
import de.rexlmanu.mlgrush.plugin.scoreboard.impl.LobbyScoreboardCreator;
import net.pluginstube.library.perk.IPerkAPI;
import net.pluginstube.library.perk.PerkCategory;
import net.pluginstube.library.perk.PerkFactory;
import org.bukkit.Bukkit;

import java.util.Map;

public class PluginStubeIntegration implements GameIntegration {

  private IPerkAPI perkAPI;

  @Override
  public void onEnable() {
    this.perkAPI = new PerkFactory().getAPI();
  }

  @Override
  public void onGameInit() {
    // Register perks
    for (int index = 0; index < StickEquipment.values().length; index++) {
      StickEquipment equipment = StickEquipment.values()[index];
      this.perkAPI.addPerk(new PluginStubePerk(PerkCategory.MLGRUSH_STICK, index + 1, equipment.displayName(), equipment.material(), equipment.cost()));
    }
    for (int index = 0; index < BlockEquipment.values().length; index++) {
      BlockEquipment equipment = BlockEquipment.values()[index];
      this.perkAPI.addPerk(new PluginStubePerk(PerkCategory.MLGRUSH_BLOCKS, index + 1, equipment.displayName(), equipment.material(), equipment.cost()));
    }

    this.perkAPI.initPerkIndex();

    // Replace scoreboard
    Map<Environment, ScoreboardCreator> map = GameManager.instance().scoreboardHandler().environmentScoreboardCreatorMap();
    map.values().stream().filter(scoreboardCreator -> scoreboardCreator instanceof LobbyScoreboardCreator).map(scoreboardCreator -> ((LobbyScoreboardCreator) scoreboardCreator)).forEach(lobbyScoreboardCreator -> lobbyScoreboardCreator.task().cancel());
    map.clear();
    map.put(Environment.LOBBY, new PluginStubeLobbyScoreboard());
    map.put(Environment.ARENA, new PluginStubeArenaScoreboard());
  }

  @Override
  public boolean isAvailable() {
    return Bukkit.getPluginManager().isPluginEnabled("library-spigot-1.0");
  }
}
