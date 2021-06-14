package de.rexlmanu.mlgrush.plugin.player;

import de.rexlmanu.mlgrush.plugin.Constants;
import de.rexlmanu.mlgrush.plugin.arena.ArenaManager;
import de.rexlmanu.mlgrush.plugin.arena.configuration.ArenaConfiguration;
import de.rexlmanu.mlgrush.plugin.detection.Detection;
import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.StickEquipment;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import fr.mrmicky.fastboard.FastBoard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
@Getter
@Accessors(fluent = true)
public class GamePlayer {

  private UUID uniqueId;
  private FastBoard fastBoard;
  @Setter
  private Environment environment;

  private Map<UUID, ArenaConfiguration.ArenaConfigurationBuilder> challengeRequests;
  private boolean creatingGame, buildMode, inspectionMode;
  private Detection detection;
  private GamePlayerData data;

  public GamePlayer(UUID uniqueId) {
    this.uniqueId = uniqueId;
    this.fastBoard = new FastBoard(this.player());
    this.environment = Environment.LOBBY;
    this.creatingGame = false;
    this.buildMode = false;
    this.inspectionMode = false;
    this.detection = new Detection();

    this.challengeRequests = ExpiringMap
      .builder()
      .expiration(3, TimeUnit.MINUTES)
      .asyncExpirationListener((key, value) -> PlayerProvider.find((UUID) key)
        .ifPresent(gamePlayer -> gamePlayer.sendMessage(String.format("Deine Anfrage an &e%s&7 ist ausgelaufen.", this.player().getName()))))
      .build();

    GameManager.instance().databaseContext().loadData(this.uniqueId).whenComplete((gamePlayerData, throwable) -> {
      if (throwable != null) {
        this.sendMessage("Wir konnten deine Daten nicht laden.");
        return;
      }
      this.data = gamePlayerData;
      GameManager.instance().statsHologramManager().show(this);
    });
  }

  public void save() {
    GameManager.instance().databaseContext().saveData(this.data);
  }

  public Player player() {
    return Bukkit.getPlayer(this.uniqueId);
  }

  public void sendMessage(String text) {
    this.player().sendMessage(MessageFormat.replaceColors(Constants.PREFIX + text));
  }

  public void sound(Sound sound, float pitch) {
    this.player().playSound(this.player().getLocation(), sound, 1f, pitch);
  }

  public void giveEquipment() {
    List<String> sorting = this.data().inventorySorting();
    for (int slot = 0; slot < sorting.size(); slot++) {
      if (sorting.get(slot) == null) continue;
      switch (sorting.get(slot)) {
        case "pickaxe":
          this.player().getInventory().setItem(slot, ArenaManager.PICKAXE);
          break;
        case "stick":
          String selectedStick = this.data().selectedStick();
          if (selectedStick == null) selectedStick = StickEquipment.values()[0].name();
          StickEquipment.valueOf(selectedStick.toUpperCase()).onEquip(this, slot);
          break;
        case "block":
          String selectedBlock = this.data().selectedBlock();
          if (selectedBlock == null) selectedBlock = BlockEquipment.values()[0].name();
          BlockEquipment.valueOf(selectedBlock.toUpperCase()).onEquip(this, slot);
          break;
        default:
          break;
      }
    }
  }
}
