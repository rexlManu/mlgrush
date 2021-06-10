package de.rexlmanu.mlgrush.plugin.player;

import de.rexlmanu.mlgrush.plugin.Constants;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.ArenaManager;
import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.StickEquipment;
import de.rexlmanu.mlgrush.plugin.game.Environment;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import eu.miopowered.repository.Key;
import fr.mrmicky.fastboard.FastBoard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@Accessors(fluent = true)
public class GamePlayer {

  private UUID uniqueId;
  private GamePlayerData data;
  private FastBoard fastBoard;
  @Setter
  private Environment environment;

  public GamePlayer(UUID uniqueId) {
    this.uniqueId = uniqueId;
    this.data = GamePlugin.getPlugin(GamePlugin.class).repository().find(Key.wrap(uniqueId)).orElse(new GamePlayerData(this.uniqueId));
    this.fastBoard = new FastBoard(this.player());
    this.environment = Environment.LOBBY;
  }

  public void save() {
    GamePlugin.getPlugin(GamePlugin.class).repository().update(this.data);
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
