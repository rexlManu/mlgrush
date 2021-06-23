package de.rexlmanu.mlgrush.plugin.equipment;

import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.pluginstube.api.perk.Perk;
import net.pluginstube.api.perk.perks.MLGRushPerks;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public enum BlockEquipment implements Equipment, Buyable {
  SANDSTONE(Material.SANDSTONE, "&aGlatter Sandstein", 0, MLGRushPerks.SANDSTONE_BLOCK),
  RED_SANDSTONE(Material.RED_SANDSTONE, "&aGlatter Roter Sandstein", 0, MLGRushPerks.REDSANDSTONE_BLOCK),
  ENDER_STONE(Material.ENDER_STONE, "&aEndstein", 2500, MLGRushPerks.ENDERSTONE_BLOCK),
  QUARTZ_STONE(Material.QUARTZ_BLOCK, "&aQuarzblock", 2500, MLGRushPerks.QUARTZSTONE_BLOCK),
  PRISMARINE(Material.PRISMARINE, "&aDunkler Prismarin", 5000, MLGRushPerks.PRISMARINE_BLOCK),
  NETHER_BRICK(Material.NETHER_BRICK, "&aNetherziegel", 5000, MLGRushPerks.NETHERBRICK_BLOCK);

  private Material material;
  private String displayName;
  private int cost;
  private Perk perk;

  @Override
  public void onEquip(GamePlayer gamePlayer, int slot) {
    gamePlayer.player().getInventory().setItem(slot, ItemStackBuilder.of(this.material).amount(64).name(this.displayName).build());
  }
}
