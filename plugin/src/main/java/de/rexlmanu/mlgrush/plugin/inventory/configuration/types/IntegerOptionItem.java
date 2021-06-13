package de.rexlmanu.mlgrush.plugin.inventory.configuration.types;

import de.rexlmanu.mlgrush.plugin.inventory.configuration.OptionItem;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Accessors(fluent = true)
@Getter
@Setter
public class IntegerOptionItem implements OptionItem<Integer> {

  private ItemStack itemStack;
  private int slot, value, maximumValue, minimumValue;

  public IntegerOptionItem(ItemStack itemStack, int slot, int value, int maximumValue, int minimumValue) {
    this.itemStack = itemStack;
    this.slot = slot;
    this.value = value;
    this.maximumValue = maximumValue;
    this.minimumValue = minimumValue;

    this.itemStack = ItemStackBuilder.of(this.itemStack).lore(
      "",
      "  &8■ &7Linksklick &8× &e+1",
      "  &8■ &7Linksklick+Shift &8× &e+10",
      "  &8■ &7Rechtsklick &8× &e-1",
      "  &8■ &7Rechtsklick+Shift &8× &e-10",
      "",
      "&8» &7Eingestellt&8: &e" + this.value
    )
      .build();

  }

  @Override
  public void interact(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    if (event.isLeftClick()) {
      this.value += event.isShiftClick() ? 10 : 1;
    }
    if (event.isRightClick()) {
      this.value -= event.isShiftClick() ? 10 : 1;
    }
    if (this.value > this.maximumValue) {
      this.value = this.maximumValue;
    }
    if (this.value < this.minimumValue) {
      this.value = this.minimumValue;
    }
    player.playSound(player.getLocation(), Sound.CLICK, 0.7f, 2f);
    inventory.setItem(this.slot, itemStack(ItemStackBuilder.of(itemStack).transformMeta(itemMeta -> {
      List<String> lore = itemMeta.getLore();
      lore.set(lore.size() - 1, MessageFormat.replaceColors("&8» &7Eingestellt&8: &e" + this.value));
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
  public Integer value() {
    return this.value;
  }
}
