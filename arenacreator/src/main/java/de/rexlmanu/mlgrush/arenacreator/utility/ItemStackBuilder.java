package de.rexlmanu.mlgrush.arenacreator.utility;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * From:https://github.com/lucko/helper/blob/master/helper/src/main/java/me/lucko/helper/item/ItemStackBuilder.java
 * <p>
 * Easily construct {@link ItemStack} instances
 */
public final class ItemStackBuilder {

  private static final ItemFlag[] ALL_FLAGS = new ItemFlag[]{
    ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
    ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS,
    ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON
  };

  private final ItemStack itemStack;

  public static ItemStackBuilder of(Material material) {
    return new ItemStackBuilder(new ItemStack(material)).hideAttributes();
  }

  public static ItemStackBuilder of(ItemStack itemStack) {
    return new ItemStackBuilder(itemStack).hideAttributes();
  }

//    public static ItemStackBuilder customSkull(String texture) {
//        ItemStackBuilder builder = of(Material.PLAYER_HEAD);
//        builder.transformMeta(itemMeta -> {
//            SkullMeta skullMeta = (SkullMeta) itemMeta;
//            var profile = new GameProfile(UUID.randomUUID(), null);
//            JsonObject object = new JsonObject();
//            JsonObject texturesObject = new JsonObject();
//            JsonObject skinObject = new JsonObject();
//            skinObject.addProperty("url", texture);
//            texturesObject.add("SKIN", skinObject);
//            object.add("textures", texturesObject);
//            String value = Base64.getEncoder().encodeToString(object.toString().getBytes(StandardCharsets.UTF_8));
//            profile.getProperties().put("textures", new Property("textures", value));
//            try {
//                Method setProfileMethod = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
//                setProfileMethod.setAccessible(true);
//                setProfileMethod.invoke(skullMeta, profile);
//            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//                e.printStackTrace();
//            }
//            builder.itemStack.setItemMeta(skullMeta);
//        });
//        return builder;
//    }


  private ItemStackBuilder(ItemStack itemStack) {
    this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
  }

  public ItemStackBuilder transform(Consumer<ItemStack> is) {
    is.accept(this.itemStack);
    return this;
  }

  public ItemStackBuilder transformMeta(Consumer<ItemMeta> meta) {
    ItemMeta m = this.itemStack.getItemMeta();
    if (m != null) {
      meta.accept(m);
      this.itemStack.setItemMeta(m);
    }
    return this;
  }

  public ItemStackBuilder name(String name) {
    return transformMeta(meta -> meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name)));
  }

  public ItemStackBuilder type(Material material) {
    return transform(itemStack -> itemStack.setType(material));
  }

  public ItemStackBuilder lore(String line) {
    return transformMeta(meta -> {
      List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
      lore.add(ChatColor.translateAlternateColorCodes('&', line));
      meta.setLore(lore);
    });
  }

  public ItemStackBuilder lore(String... lines) {
    return transformMeta(meta -> {
      List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
      for (String line : lines) {
        lore.add(ChatColor.translateAlternateColorCodes('&', line));
      }
      meta.setLore(lore);
    });
  }

  public ItemStackBuilder lore(Iterable<String> lines) {
    return transformMeta(meta -> {
      List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
      for (String line : lines) {
        lore.add(ChatColor.translateAlternateColorCodes('&', line));
      }
      meta.setLore(lore);
    });
  }

  public ItemStackBuilder clearLore() {
    return transformMeta(meta -> meta.setLore(new ArrayList<>()));
  }

  public ItemStackBuilder durability(int durability) {
    return transform(itemStack -> itemStack.setDurability((short) durability));
  }

  public ItemStackBuilder data(int data) {
    return durability(data);
  }

  public ItemStackBuilder amount(int amount) {
    return transform(itemStack -> itemStack.setAmount(amount));
  }

  public ItemStackBuilder enchant(Enchantment enchantment, int level) {
    return transform(itemStack -> itemStack.addUnsafeEnchantment(enchantment, level));
  }

  public ItemStackBuilder enchant(Enchantment enchantment) {
    return transform(itemStack -> itemStack.addUnsafeEnchantment(enchantment, 1));
  }

  public ItemStackBuilder clearEnchantments() {
    return transform(itemStack -> itemStack.getEnchantments().keySet().forEach(itemStack::removeEnchantment));
  }

  public ItemStackBuilder flag(ItemFlag... flags) {
    return transformMeta(meta -> meta.addItemFlags(flags));
  }

  public ItemStackBuilder unflag(ItemFlag... flags) {
    return transformMeta(meta -> meta.removeItemFlags(flags));
  }

  public ItemStackBuilder hideAttributes() {
    return flag(ALL_FLAGS);
  }

  public ItemStackBuilder showAttributes() {
    return unflag(ALL_FLAGS);
  }

  public ItemStackBuilder color(Color color) {
    return transform(itemStack -> {
      Material type = itemStack.getType();
      if (type == Material.LEATHER_BOOTS || type == Material.LEATHER_CHESTPLATE || type == Material.LEATHER_HELMET || type == Material.LEATHER_LEGGINGS) {
        LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(color);
        itemStack.setItemMeta(meta);
      }
    });
  }

  public ItemStackBuilder breakable(boolean flag) {
    return transformMeta(meta -> meta.spigot().setUnbreakable(!flag));
  }

  public ItemStackBuilder apply(Consumer<ItemStackBuilder> consumer) {
    consumer.accept(this);
    return this;
  }

  public ItemStack build() {
    return this.itemStack;
  }

}