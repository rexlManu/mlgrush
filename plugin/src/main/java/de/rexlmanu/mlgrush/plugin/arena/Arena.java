package de.rexlmanu.mlgrush.plugin.arena;

import com.cryptomorin.xseries.messages.ActionBar;
import de.rexlmanu.mlgrush.arenalib.ArenaPosition;
import de.rexlmanu.mlgrush.arenalib.ArenaTemplate;
import de.rexlmanu.mlgrush.arenalib.Position;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.equipment.BlockEquipment;
import de.rexlmanu.mlgrush.plugin.equipment.StickEquipment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.PlayerUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Builder
@Accessors(fluent = true)
@Getter
@Setter
public class Arena implements Listener, Runnable {

    private static final ItemStack PICKAXE = ItemStackBuilder
            .of(Material.STONE_PICKAXE)
            .name("&eSpitzhacke")
            .breakable(false)
            .hideAttributes()
            .enchant(Enchantment.DIG_SPEED, 1)
            .build();

    public static final int RED_TEAM = 0;
    public static final int BLUE_TEAM = 1;
    public static final int SPAWN_PROTECTION = 3;

    private Location startPoint, firstCorner, secondCorner, teamRedSpawn, teamBlueSpawn;
    private Map<Player, Location> spawnLocation = new HashMap<>();
    private List<Block> placedBlocks = new ArrayList<>();
    private GamePlayer winner;
    private int winnerTeam;
    private int animationTick = 0;

    private double maxX, maxY, maxZ, minX, minY, minZ;
    private BukkitTask task, scoreboardTask;

    private ArenaTemplate template;

    private List<GamePlayer> players;

    private int redPoints, bluePoints;
    private long gameStart;

    public Arena(List<GamePlayer> players) {
        this.players = players;
        this.redPoints = 0;
        this.bluePoints = 0;
        this.gameStart = System.currentTimeMillis();
    }

    private Location getLocation(String key) {
        Position position = this.template.positionMap().get(key);
        Location location = this.startPoint.clone().add(position.x(), position.y(), position.z());
        location.setYaw(position.yaw());
        location.setPitch(position.pitch());
        return location;
    }

    public void start() {
        this.firstCorner = this.getLocation(ArenaPosition.FIRST_CORNER);
        this.secondCorner = this.getLocation(ArenaPosition.SECOND_CORNER);
        this.teamRedSpawn = this.getLocation(ArenaPosition.RED_SPAWN);
        this.teamBlueSpawn = this.getLocation(ArenaPosition.BLUE_SPAWN);
        this.maxX = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
        this.maxY = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY());
        this.maxZ = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());

        this.minX = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
        this.minY = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
        this.minZ = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), this, 0, 1);
        this.gameStart = System.currentTimeMillis();

        Collections.shuffle(this.players);
        Bukkit.getScheduler().runTask(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> {
            for (int i = 0; i < this.players.size(); i++) {
                GamePlayer gamePlayer = this.players.get(i);
                Player player = gamePlayer.player();
                Bukkit.getOnlinePlayers().stream().filter(target -> this.players.stream()
                        .noneMatch(gamePlayer1 -> gamePlayer1.player().equals(target))).forEach(player::hidePlayer);
                PlayerUtils.resetPlayer(player);
                if (i == RED_TEAM) {
                    player.teleport(this.teamRedSpawn);
                    this.spawnLocation.put(player, this.teamRedSpawn);
                } else {
                    player.teleport(this.teamBlueSpawn);
                    this.spawnLocation.put(player, this.teamBlueSpawn);
                }
                this.giveKit(gamePlayer);
                gamePlayer.arenaStatistics(new ArenaStatistics(0, 0, 0, null));
                player.setGameMode(GameMode.SURVIVAL);
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                Scoreboard scoreboard = player.getScoreboard();
                Team red = scoreboard.registerNewTeam("red");
                red.setPrefix(MessageFormat.replaceColors("&c"));
                Team blue = scoreboard.registerNewTeam("blue");
                blue.setPrefix(MessageFormat.replaceColors("&b"));
                for (int teamIndex = 0; teamIndex < this.players.size(); teamIndex++) {
                    switch (teamIndex) {
                        case RED_TEAM:
                            red.addEntry(this.players.get(teamIndex).player().getName());
                            break;
                        case BLUE_TEAM:
                            blue.addEntry(this.players.get(teamIndex).player().getName());
                            break;
                    }
                }
                player.setScoreboard(scoreboard);

                sendScoreboard(gamePlayer);
            }

            this.scoreboardTask = Bukkit.getScheduler().runTaskTimerAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> {
                this.players.forEach(this::sendScoreboard);
                this.animationTick++;
                // kills, tode, blöcke
                if (this.animationTick == 3) this.animationTick = 0;
            }, 0, 20 * 4);
        });
    }

    private void giveKit(GamePlayer gamePlayer) {
        List<String> sorting = gamePlayer.data().inventorySorting();
        for (int slot = 0; slot < sorting.size(); slot++) {
            if (sorting.get(slot) == null) continue;
            switch (sorting.get(slot)) {
                case "pickaxe":
                    gamePlayer.player().getInventory().setItem(slot, PICKAXE);
                    break;
                case "stick":
                    String selectedStick = gamePlayer.data().selectedStick();
                    if (selectedStick == null) selectedStick = StickEquipment.values()[0].name();
                    StickEquipment.valueOf(selectedStick.toUpperCase()).onEquip(gamePlayer, slot);
                    break;
                case "block":
                    String selectedBlock = gamePlayer.data().selectedBlock();
                    if (selectedBlock == null) selectedBlock = BlockEquipment.values()[0].name();
                    BlockEquipment.valueOf(selectedBlock.toUpperCase()).onEquip(gamePlayer, slot);
                    break;
                default:
                    break;
            }

        }
    }

    private boolean inArena(Player player) {
        return this.players().stream().map(GamePlayer::player).anyMatch(p -> p.equals(player));
    }

    private boolean inArena(Location location) {
        return location.getX() > minX && location.getX() < maxX
                && location.getY() > minY && location.getY() < maxY
                && location.getZ() > minZ && location.getZ() < maxZ;
    }

    private GamePlayer getPlayer(Player player) {
        return this.players.stream().filter(gamePlayer -> gamePlayer.player().equals(player)).findAny().get();
    }

    @EventHandler
    public void handle(BlockPlaceEvent event) {
        if (!this.inArena(event.getPlayer())) return;
        if (!this.inArena(event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }
        // height limit
        if (event.getBlock().getLocation().getY() >= this.teamBlueSpawn.getY()) {
            event.setCancelled(true);
            return;
        }
        GamePlayer player = this.getPlayer(event.getPlayer());
        // spawn protection
        if (this.players.get(RED_TEAM).equals(player)) {
            // team red
            if (this.isInProtection(teamRedSpawn, event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }
        } else {
            // team blue
            if (this.isInProtection(teamBlueSpawn, event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }
        }

        this.placedBlocks.add(event.getBlock());
        player.arenaStatistics().addBlock();
    }

    private boolean isInProtection(Location teamSpawn, Location target) {
        double x = teamSpawn.getX();
        double z = teamSpawn.getZ();

        double firstX = x + 3;
        double firstZ = z + 3;
        double secondX = x - 3;
        double secondZ = z - 3;

        double x1 = Math.min(firstX, secondX);
        double z1 = Math.min(firstZ, secondZ);

        double x2 = Math.max(firstX, secondX);
        double z2 = Math.max(firstZ, secondZ);

        return target.getX() > x1 && target.getX() < x2 && target.getZ() > z1 && target.getZ() < z2;
    }

    @EventHandler
    public void handle(BlockBreakEvent event) {
        if (!this.inArena(event.getPlayer())) return;
        if (!this.inArena(event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (event.getBlock().getType().equals(Material.BED) || event.getBlock().getType().equals(Material.BED_BLOCK)) {
            event.setCancelled(true);
            double distanceToRed = event.getBlock().getLocation().distance(teamRedSpawn);
            double distanceToBlue = event.getBlock().getLocation().distance(teamBlueSpawn);
            GamePlayer player = this.getPlayer(event.getPlayer());
            if (distanceToRed < distanceToBlue) {
                if (this.players.get(RED_TEAM).equals(player)) return;
                // team red bed
                this.givePoint(BLUE_TEAM);
            } else {
                if (this.players.get(BLUE_TEAM).equals(player)) return;
                // team blue bed
                this.givePoint(RED_TEAM);
            }
            return;
        }
        if (!this.placedBlocks.contains(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
        event.getBlock().setType(Material.AIR);
        event.getBlock().getLocation().getWorld().playSound(event.getBlock().getLocation(), Sound.DIG_STONE, 1f, 1f);
        this.placedBlocks.remove(event.getBlock());
    }

    private void givePoint(int team) {
        switch (team) {
            case BLUE_TEAM:
                this.bluePoints++;
                break;
            case RED_TEAM:
                this.redPoints++;
                break;
        }
        this.resetMap();

        this.players.forEach(gamePlayer -> {
            this.killPlayer(gamePlayer.player(), true);
            gamePlayer.sound(Sound.ORB_PICKUP, 2f);
        });

        if (this.checkForWinner()) {
            GameManager.instance().arenaContainer().finish(this);
        }
    }

    private boolean checkForWinner() {
        if (this.redPoints == 10) {
            this.winner = this.players.get(RED_TEAM);
            this.winnerTeam = RED_TEAM;
            return true;
        } else if (this.bluePoints == 10) {
            this.winner = this.players.get(BLUE_TEAM);
            this.winnerTeam = BLUE_TEAM;
            return true;
        }
        return false;
    }

    public void end() {
        // reseting map
        Bukkit.getScheduler().runTask(GamePlugin.getProvidingPlugin(GamePlugin.class), () -> {
            for (int x = 0; x < (maxX - minX); x++) {
                for (int y = 0; y < (maxY - minY); y++) {
                    for (int z = 0; z < (maxZ - minZ); z++) {
                        Location add = this.startPoint.clone().add(x, y, z);
                        if (!add.getBlock().getType().equals(Material.AIR)) {
                            add.getBlock().setType(Material.AIR);
                        }
                    }
                }
            }
        });

        HandlerList.unregisterAll(this);
        this.task.cancel();
        this.scoreboardTask.cancel();
    }

    private void resetMap() {
        this.placedBlocks.forEach(block -> block.setType(Material.AIR));
        this.placedBlocks.clear();
    }

    @EventHandler
    public void handle(PlayerMoveEvent event) {
        if (!this.inArena(event.getPlayer())) return;
        if (event.getTo().getX() == event.getFrom().getX()
                && event.getTo().getY() == event.getFrom().getY()
                && event.getTo().getZ() == event.getFrom().getZ()) return;

        if (!this.inArena(event.getTo())) {
            this.killPlayer(event.getPlayer(), false);
            return;
        }
    }

    private void killPlayer(Player player, boolean ignoreStats) {
        GamePlayer gamePlayer = this.getPlayer(player);
        if (!ignoreStats && gamePlayer.arenaStatistics() != null) {
            gamePlayer.arenaStatistics().addDeath();
            if (gamePlayer.arenaStatistics().lastHitter() != null) {
                this.getPlayer(gamePlayer.arenaStatistics().lastHitter()).arenaStatistics().addKill();
            }
        }
        gamePlayer.arenaStatistics().lastHitter(null);
        player.teleport(this.spawnLocation.get(player));
        player.closeInventory();
        player.getInventory().clear();
        this.giveKit(gamePlayer);
        player.setVelocity(new Vector(0, 0, 0));
    }

    @EventHandler
    public void handle(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!this.inArena(player)) return;
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            event.setCancelled(true);
            return;
        }
        event.setDamage(0);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(PlayerQuitEvent event) {
        if (!this.inArena(event.getPlayer())) return;
        GamePlayer leaver = this.getPlayer(event.getPlayer());
        if (this.players.get(RED_TEAM).equals(leaver)) {
            // blue team wins
            this.winnerTeam = BLUE_TEAM;
            this.winner = this.players.get(BLUE_TEAM);
        } else {
            // red team wins
            this.winnerTeam = RED_TEAM;
            this.winner = this.players.get(RED_TEAM);
        }
        GameManager.instance().arenaContainer().finish(this);
    }

    @EventHandler
    public void handle(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player damager = (Player) event.getDamager();
        Player player = (Player) event.getEntity();
        if (!this.inArena(player) || !this.inArena(damager)) return;
        this.getPlayer(player).arenaStatistics().lastHitter(damager);
    }

    @EventHandler
    public void handle(PlayerBedEnterEvent event) {
        if (!this.inArena(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @Override
    public void run() {
        // 1 x assad x ad
        long seconds = (System.currentTimeMillis() - this.gameStart) / 1000;
        this.players.forEach(gamePlayer -> {
            ActionBar.sendActionBar(gamePlayer.player(), MessageFormat.replaceColors(
                    String.format(
                            "&b%s &8■ &7%02d:%02d:%02d &8■ &c%s",
                            this.bluePoints,
                            seconds / 3600, (seconds % 3600) / 60, seconds % 60,
                            this.redPoints
                    )
            ));
        });
    }

    private void sendScoreboard(GamePlayer gamePlayer) {
        GamePlayer target = this.players.get(BLUE_TEAM);
        if (!this.players.get(RED_TEAM).equals(gamePlayer)) {
            target = this.players.get(RED_TEAM);
        }
        String type = "";
        int value = 0;
        if(gamePlayer.arenaStatistics() != null) {
            switch (animationTick) {
                case 0:
                    type = "Tötungen";
                    value = gamePlayer.arenaStatistics().kills();
                    break;
                case 1:
                    type = "Tode";
                    value = gamePlayer.arenaStatistics().deaths();
                    break;
                case 2:
                    type = "platzierten Blöcke";
                    value = gamePlayer.arenaStatistics().blocks();
                    break;
                default:
                    break;
            }
        }
        gamePlayer.fastBoard().updateLines(Stream.of(
                "",
                "&8■ &7Dein Gegner",
                "&8 » &e" + target.player().getName(),
                "",
                "&8■ &7Deine " + type,
                "&8 » &e" + value,
                "",
                "&8■ &7Arena",
                "&8 » &e" + this.template.name(),
                ""
        ).map(MessageFormat::replaceColors).collect(Collectors.toList()));
    }

    @EventHandler
    public void handle(AsyncPlayerChatEvent event) {
        if (!this.inArena(event.getPlayer())) return;
        GamePlayer player = getPlayer(event.getPlayer());
        event.setCancelled(true);
        String message = MessageFormat.replaceColors(String.format("&e%s &8» &7", player.player().getName())) + event.getMessage();

        this.players.forEach(target -> target.player().sendMessage(message));
    }
}
