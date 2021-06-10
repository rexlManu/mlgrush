package de.rexlmanu.mlgrush.plugin.arena.world;

import de.rexlmanu.mlgrush.arenalib.ArenaTemplate;
import de.rexlmanu.mlgrush.arenalib.Ingredient;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.configuration.ArenaConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;

public class ArenaWriter {

  public static void generateTemplate(ArenaConfiguration configuration) {
    Location startPoint = configuration.startPoint();
    ArenaTemplate template = configuration.arenaTemplate();
    int[][][] layout = template.layout();
    List<Ingredient> ingredients = template.ingredients();
    long start = System.currentTimeMillis();
    Bukkit.getScheduler().runTask(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> {
      for (int x = 0; x < layout.length; x++) {
        for (int y = 0; y < layout[x].length; y++) {
          for (int z = 0; z < layout[x][y].length; z++) {
            int code = layout[x][y][z];
            Location location = new Location(startPoint.getWorld(), startPoint.getX() + x, startPoint.getY() + y, startPoint.getZ() + z);
            ingredients.stream().filter(ingredient -> ingredient.code() == code).findAny().ifPresent(ingredient -> {
              Block block = location.getBlock();
              block.setType(Material.valueOf(ingredient.material().toUpperCase()));
              block.setData((byte) ingredient.data());
            });
          }
        }
      }
      System.out.println(String.format("Map generated in %sms.", System.currentTimeMillis() - start));
    });
  }

}
