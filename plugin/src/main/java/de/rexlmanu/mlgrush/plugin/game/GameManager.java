package de.rexlmanu.mlgrush.plugin.game;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.ArenaContainer;
import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.EquipmentMob;
import de.rexlmanu.mlgrush.plugin.equipment.StickEquipment;
import de.rexlmanu.mlgrush.plugin.game.environment.LobbyEnvironment;
import de.rexlmanu.mlgrush.plugin.game.npc.InteractiveMob;
import de.rexlmanu.mlgrush.plugin.inventory.ShopInventory;
import de.rexlmanu.mlgrush.plugin.location.LocationProvider;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.queue.QueueController;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main logic will get manages from here
 */
@Accessors(fluent = true)
@Getter
public class GameManager {

    private static final List<EquipmentMob> EQUIPMENT_MOBS = Arrays.asList(
            EquipmentMob.builder()
                    .entityType(EntityType.ZOMBIE)
                    .lines(Arrays.asList("&8» &eBlock", "&7Ändere den Typ deiner Blöcke."))
                    .inventoryName("&eBlöcke")
                    .locationName("block-change-npc")
                    .elements(BlockEquipment.values())
                    .build(),
            EquipmentMob.builder()
                    .entityType(EntityType.VILLAGER)
                    .lines(Arrays.asList("&8» &eStick", "&7Ändere den Stick, mit dem du kämpfst."))
                    .inventoryName("&eStick")
                    .locationName("stick-change-npc")
                    .elements(StickEquipment.values())
                    .build()
    );

    public static void create() {
        new GameManager();
    }

    @Getter
    private static GameManager instance;


    private LocationProvider locationProvider;
    private GameEnvironment lobbyEnvironment;
    private QueueController queueController;
    private ArenaContainer arenaContainer;
    private List<InteractiveMob> interactiveMobs;

    private GameManager() {
        GameManager.instance = this;
        File dataFolder = GamePlugin.getPlugin(GamePlugin.class).getDataFolder();
        this.locationProvider = new LocationProvider(dataFolder);
        this.lobbyEnvironment = new LobbyEnvironment();
        this.interactiveMobs = new ArrayList<>();

        Bukkit.getWorlds().forEach(world -> {
            world.setDifficulty(Difficulty.NORMAL);
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doMobSpawning", "false");
        });
        this.arenaContainer = new ArenaContainer();
        this.queueController = new QueueController();

        this.locationProvider.get("queue-npc").ifPresent(location -> this.interactiveMobs.add(new InteractiveMob(EntityType.WITCH, Arrays.asList(
                "&8» &eQueue",
                "&7Suche nach einem Gegner."
        ), player -> {
            if (player.isIngame()) {
                player.sendMessage("Du bist bereits in einem Spiel.");
                return;
            }
            if (this.queueController.inQueue(player)) {
                this.queueController.playerQueue().remove(player);
                player.sendMessage("Du hast die &eWarteschlange &7verlassen.");
                this.updateScoreboardAll();
                return;
            }
            this.queueController.playerQueue().offer(player);
            player.sendMessage("Du hast die &eWarteschlange &7betreten.");
            this.updateScoreboardAll();
        }).create(location)));

        EQUIPMENT_MOBS
                .forEach(equipmentMob -> this.locationProvider.get(equipmentMob.locationName())
                        .ifPresent(location -> this.interactiveMobs.add(
                                new InteractiveMob(equipmentMob.entityType(), equipmentMob.lines(), player ->
                                        new ShopInventory(player, equipmentMob.inventoryName(), equipmentMob.elements()))
                                        .create(location)
                        )));
    }

    public void onDisable() {
        this.interactiveMobs.forEach(InteractiveMob::remove);
    }

    public void updateScoreboardAll() {
        PlayerProvider.PLAYERS.stream().filter(GamePlayer::isInLobby).forEach(this::updateScoreboard);
    }

    public void updateScoreboard(GamePlayer gamePlayer) {
        gamePlayer.fastBoard().updateLines(Stream.of(
                "",
                "&8■ &7Warteschlange",
                "&8 » &e" + GameManager.instance().queueController().playerQueue().size() + " Spieler",
                "",
                "&8■ &7Spieler im Spiel",
                "&8 » &e" + PlayerProvider.PLAYERS.stream().filter(GamePlayer::isIngame).count() + " Spieler",
                ""
        ).map(MessageFormat::replaceColors).collect(Collectors.toList()));
    }

    public void updateTablist(GamePlayer gamePlayer) {
        Player player = gamePlayer.player();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Team teamLobby = scoreboard.registerNewTeam("1-lobby");
        teamLobby.setPrefix(MessageFormat.replaceColors("&7"));
        Team teamIngame = scoreboard.registerNewTeam("2-ingame");
        teamIngame.setPrefix(MessageFormat.replaceColors("&8"));
        PlayerProvider.PLAYERS.forEach(target -> {
            if(target.isIngame()) {
                teamIngame.addEntry(target.player().getName());
            }else {
                teamLobby.addEntry(target.player().getName());
            }
        });

        player.setScoreboard(scoreboard);
    }
}
