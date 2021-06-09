package de.rexlmanu.mlgrush.plugin.arena;

import com.cryptomorin.xseries.messages.Titles;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.inventory.ArenaChoosingInventory;
import de.rexlmanu.mlgrush.plugin.arena.world.ChunkArenaGenerator;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.Statistics;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.PlayerUtils;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.List;

@Accessors(fluent = true)
@Getter
public class ArenaContainer {

    private static final String ARENA_WORLD = "arena";

    private List<Arena> activeArenas = new LinkedList<>();

    private World world;
    private TemplateHandler templateLoader;

    public ArenaContainer() {
        this.world = Bukkit.createWorld(
                WorldCreator
                        .name(ARENA_WORLD)
                        .environment(World.Environment.NORMAL)
                        .generator(new ChunkArenaGenerator())
                        .generateStructures(false)
        );
        this.templateLoader = new TemplateHandler();

        this.world.setDifficulty(Difficulty.PEACEFUL);
        this.world.setFullTime(2000);
        this.world.setGameRuleValue("doDaylightCycle", "false");
        this.world.setGameRuleValue("doMobSpawning", "false");
    }

    public void create(List<GamePlayer> players) {
        Arena arena = new Arena(players);
        this.activeArenas.add(arena);
        players.forEach(gamePlayer -> gamePlayer.arena(arena));
        GameManager.instance().updateScoreboardAll();
        ArenaChoosingInventory.create(arena);
    }

    public void start(Arena arena) {
        JavaPlugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ArenaWriter.writeArena(arena);
            Bukkit.getPluginManager().registerEvents(arena, plugin);
            arena.start();
        });
    }

    public void abort(Arena arena) {
        arena.players().forEach(gamePlayer -> {
            gamePlayer.arena(null);
            gamePlayer.sendMessage("Das Spiel wurde abgebrochen.");
        });
        this.activeArenas.remove(arena);
    }

    public void finish(Arena arena) {
        arena.players().forEach(gamePlayer -> {
            Player player = gamePlayer.player();
            PlayerUtils.resetPlayer(player);
            GameManager.instance().locationProvider().get("spawn").ifPresent(location -> player.teleport(location));
            Titles.sendTitle(player, 5, 20, 10,
                    MessageFormat.replaceColors(arena.winnerTeam() == Arena.RED_TEAM ? "&bTeam Blau" : "&cTeam Rot"),
                    MessageFormat.replaceColors("&7hat gewonnen.")
            );
            gamePlayer.sound(Sound.LEVEL_UP, 1f);
            gamePlayer.arena(null);
            Statistics statistics = gamePlayer.data().statistics();
            ArenaStatistics arenaStatistics = gamePlayer.arenaStatistics();
            player.sendMessage("");
            gamePlayer.sendMessage("Deine Statistiken haben sich folgend verändert:");
            player.sendMessage("");
            player.sendMessage(MessageFormat.replaceColors(String.format("  &7Tötungen &8» &e%s &8(&e+%s&7&8)", statistics.kills(), arenaStatistics.kills())));
            player.sendMessage(MessageFormat.replaceColors(String.format("  &7Tode &8» &e%s &8(&e+%s&7&8)", statistics.deaths(), arenaStatistics.deaths())));
            player.sendMessage(MessageFormat.replaceColors(String.format("  &7Gespielte Spiele &8» &e%s &8(&e+%s&7&8)", statistics.games(), 1)));
            player.sendMessage(MessageFormat.replaceColors(String.format("  &7Gewonnene Spiele &8» &e%s &8(&e+%s&7&8)", statistics.wins(), gamePlayer.equals(arena.winner()) ? 1 : 0)));
            player.sendMessage(MessageFormat.replaceColors(String.format("  &7Blöcke &8» &e%s &8(&e+%s&7&8)", statistics.blocks(), arenaStatistics.blocks())));
            player.sendMessage("");
            statistics
                    .addBlocks(arenaStatistics.blocks())
                    .addDeaths(arenaStatistics.deaths())
                    .addKills(arenaStatistics.kills());
            if (gamePlayer.equals(arena.winner())) {
                statistics.addWin();
            }
            statistics.addGame();
            gamePlayer.arenaStatistics(null);
            Bukkit.getOnlinePlayers().forEach(player::showPlayer);
            GameManager.instance().updateTablist(gamePlayer);
        });
        arena.end();

        this.activeArenas.remove(arena);
        GameManager.instance().updateScoreboardAll();
    }
}
