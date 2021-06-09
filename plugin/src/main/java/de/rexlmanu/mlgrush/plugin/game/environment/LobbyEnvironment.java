package de.rexlmanu.mlgrush.plugin.game.environment;

import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.event.EventCancel;
import de.rexlmanu.mlgrush.plugin.game.GameEnvironment;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import de.rexlmanu.mlgrush.plugin.utility.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.Arrays;

public class LobbyEnvironment implements GameEnvironment, Listener {

    public LobbyEnvironment() {
        Arrays.asList(
                FoodLevelChangeEvent.class,
                WeatherChangeEvent.class,
                PlayerDropItemEvent.class,
                PlayerPickupItemEvent.class,
                PlayerAchievementAwardedEvent.class,
                PlayerArmorStandManipulateEvent.class,
                PlayerBedEnterEvent.class,
                PlayerItemDamageEvent.class,
                // BlockBreakEvent.class,
                // BlockPlaceEvent.class,
                BlockPhysicsEvent.class,
                BlockSpreadEvent.class,
                BlockGrowEvent.class,
                BlockIgniteEvent.class,
                EntityCombustEvent.class
        ).forEach(EventCancel::on);

        Bukkit.getPluginManager().registerEvents(this, GamePlugin.getProvidingPlugin(GamePlugin.class));
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        PlayerUtils.resetPlayer(player);
        GamePlayer gamePlayer = new GamePlayer(player.getUniqueId());
        PlayerProvider.PLAYERS.add(gamePlayer);
        GameManager.instance().locationProvider().get("spawn").ifPresent(player::teleport);
        gamePlayer.fastBoard().updateTitle(MessageFormat.replaceColors("&e&lMLGRush"));
        GameManager.instance().updateScoreboard(gamePlayer);
        PlayerProvider.PLAYERS.stream().filter(GamePlayer::isInLobby).forEach(target -> GameManager.instance().updateTablist(target));
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();

        PlayerProvider.find(player.getUniqueId()).ifPresent(gamePlayer -> {
            gamePlayer.save();
            PlayerProvider.PLAYERS.remove(gamePlayer);
        });
    }

    @EventHandler
    public void handle(AsyncPlayerChatEvent event) {
        PlayerProvider.find(event.getPlayer().getUniqueId()).filter(GamePlayer::isInLobby).ifPresent(gamePlayer -> {
            event.setCancelled(true);
            String message = MessageFormat.replaceColors(String.format("&e%s &8» &7", gamePlayer.player().getName())) + event.getMessage();

            PlayerProvider.PLAYERS.stream().filter(GamePlayer::isInLobby).forEach(target -> {
                target.player().sendMessage(message);
            });
        });
    }

    @EventHandler
    public void handle(CreatureSpawnEvent event) {
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(BlockBreakEvent event) {
        PlayerProvider.find(event.getPlayer().getUniqueId()).filter(GamePlayer::isInLobby).ifPresent(gamePlayer -> {
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void handle(BlockPlaceEvent event) {
        PlayerProvider.find(event.getPlayer().getUniqueId()).filter(GamePlayer::isInLobby).ifPresent(gamePlayer -> {
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void handle(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
            return;
        }
        Player player = (Player) event.getEntity();
        PlayerProvider.find(player.getUniqueId()).filter(GamePlayer::isInLobby).ifPresent(gamePlayer -> {
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void handle(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        PlayerProvider.find(player.getUniqueId()).filter(GamePlayer::isInLobby).ifPresent(gamePlayer -> {
            event.setCancelled(true);
        });
    }
}
