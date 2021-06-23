package de.rexlmanu.mlgrush.plugin.equipment;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.pluginstube.api.perk.Perk;
import net.pluginstube.api.perk.perks.MLGRushPerks;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public enum StickEquipment implements Equipment, Buyable {
  WOOD(Material.STICK, "&aNormaler Stock", 0, MLGRushPerks.WOOD_STICK),
  BLAZE(Material.BLAZE_ROD, "&aLohenrute", 0, MLGRushPerks.BLAZE_STICK),
  BONE(Material.BONE, "&aKnochen", 2500, MLGRushPerks.BONE_STICK),
  HOE(Material.IRON_HOE, "&aEisenhoe", 2500, MLGRushPerks.IRONHOE_STICK),
  IRON_SWORD(Material.IRON_SWORD, "&aEisenschwert", 5000, MLGRushPerks.IRONSWORD_STICK),
  DIAMOND_SWORD(Material.DIAMOND_SWORD, "&aDiamantschwert", 7500, MLGRushPerks.DIAMONDSWORD_STICK);

  private Material material;
  private String displayName;
  private int cost;
  private Perk perk;

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
