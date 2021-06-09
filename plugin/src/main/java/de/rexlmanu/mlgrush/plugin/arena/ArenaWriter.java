package de.rexlmanu.mlgrush.plugin.arena;

import de.rexlmanu.mlgrush.arenalib.ArenaTemplate;
import de.rexlmanu.mlgrush.arenalib.Ingredient;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;

public class ArenaWriter {

    public static final int HEIGHT = 75;
    public static final int SPACE_X = 100;
    public static final int SPACE_Z = 100;

    public static void writeArena(Arena arena) {
        ArenaContainer container = GameManager.instance().arenaContainer();
        World world = container.world();

        arena.startPoint(new Location(world, ArenaWriter.getFreeX(), HEIGHT, SPACE_Z));
        ArenaWriter.generateTemplate(arena);
    }

    private static void generateTemplate(Arena arena) {
        Location startPoint = arena.startPoint();
        ArenaTemplate template = arena.template();
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

    private static int getFreeX() {
        Location baseLocation = GameManager.instance().arenaContainer().world().getSpawnLocation();
        int x = baseLocation.getBlockX();
        while (true) {
            int finalX = x;
            if (GameManager.instance().arenaContainer().activeArenas().stream().filter(arena -> arena.startPoint() != null)
                    .noneMatch(target -> target.startPoint().getBlockX() == finalX)) {
                break;
            }
            x += SPACE_X;
        }
        return x;
    }

}
