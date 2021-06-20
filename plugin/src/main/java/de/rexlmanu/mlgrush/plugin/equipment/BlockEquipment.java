package de.rexlmanu.mlgrush.plugin.equipment;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public enum BlockEquipment implements Equipment, Buyable {
  SANDSTONE(Material.SANDSTONE, "&dGlatter Sandstein", 0),
  RED_SANDSTONE(Material.RED_SANDSTONE, "&dGlatter Roter Sandstein", 0),
  ENDER_STONE(Material.ENDER_STONE, "&dEndstein", 2500),
  QUARTZ_STONE(Material.QUARTZ_BLOCK, "&dQuarzblock", 2500),
  PRISMARINE(Material.PRISMARINE, "&dDunkler Prismarin", 5000),
  NETHER_BRICK(Material.NETHER_BRICK, "&dNetherziegel", 5000);

  private Material material;
  private String displayName;
  private int cost;

  @Override
  public void onEquip(GamePlayer gamePlayer, int slot) {
    gamePlayer.player().getInventory().setItem(slot, ItemStackBuilder.of(this.material).amount(64).name(this.displayName).build());
  }
}
