package de.rexlmanu.mlgrush.plugin.inventory.configuration;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface OptionItem<T> {
  ItemStack itemStack();

  int slot();

  T value();

  void interact(InventoryClickEvent event);

}
