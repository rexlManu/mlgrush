package de.rexlmanu.mlgrush.plugin.equipment;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public enum StickEquipment implements Equipment, Buyable {
  WOOD(Material.STICK, "&dNormaler Stock", 0),
  BLAZE(Material.BLAZE_ROD, "&dLohenrute", 0),
  BONE(Material.BONE, "&dKnochen", 2500),
  HOE(Material.IRON_HOE, "&dEisenhoe", 2500),
  IRON_SWORD(Material.IRON_SWORD, "&dEisenschwert", 5000),
  DIAMOND_SWORD(Material.DIAMOND_SWORD, "&dDiamantschwert", 7500);

  private Material material;
  private String displayName;
  private int cost;

  @Override
  public void onEquip(GamePlayer gamePlayer, int slot) {
    gamePlayer.player().getInventory().setItem(slot, ItemStackBuilder
      .of(this.material)
      .amount(1)
      .name(this.displayName)
      .enchant(Enchantment.KNOCKBACK, 1)
      .build());
  }
}
