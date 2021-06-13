package de.rexlmanu.mlgrush.plugin.inventory.configuration.types;

import de.rexlmanu.mlgrush.plugin.inventory.configuration.OptionItem;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Accessors(fluent = true)
@Setter
public class BooleanOptionItem implements OptionItem<Boolean> {
  private ItemStack itemStack;
  private int slot;
  private boolean value;

  public BooleanOptionItem(ItemStack itemStack, int slot, boolean value) {
    this.itemStack = itemStack;
    this.slot = slot;
    this.value = value;

    this.itemStack = ItemStackBuilder.of(this.itemStack).lore(this.value ? "&r  &a&lAKTIVIERT" : "&r  &c&lDEAKTIVIERT").build();
  }

  @Override
  public void interact(InventoryClickEvent event) {
    Inventory inventory = event.getClickedInventory();
    Player player = (Player) event.getWhoClicked();
    player.playSound(player.getLocation(), Sound.CLICK, 0.7f, 2f);
    this.value = !this.value;
    inventory.setItem(this.slot, itemStack(ItemStackBuilder.of(itemStack).transformMeta(itemMeta -> {
      List<String> lore = itemMeta.getLore();
      lore.set(lore.size() - 1, MessageFormat.replaceColors(this.value ? "&r  &a&lAKTIVIERT" : "&r  &c&lDEAKTIVIERT"));
      itemMeta.setLore(lore);
    }).build()).itemStack());
  }

  @Override
  public ItemStack itemStack() {
    return this.itemStack;
  }

  @Override
  public int slot() {
    return this.slot;
  }

  @Override
  public Boolean value() {
    return this.value;
  }
}
