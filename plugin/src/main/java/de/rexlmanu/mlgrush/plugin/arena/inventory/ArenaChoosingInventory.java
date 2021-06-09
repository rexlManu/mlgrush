package de.rexlmanu.mlgrush.plugin.arena.inventory;

import de.rexlmanu.mlgrush.arenalib.ArenaTemplate;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import de.rexlmanu.mlgrush.plugin.arena.Arena;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayer;
import de.rexlmanu.mlgrush.plugin.player.PlayerProvider;
import de.rexlmanu.mlgrush.plugin.utility.ItemStackBuilder;
import de.rexlmanu.mlgrush.plugin.utility.MessageFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ArenaChoosingInventory implements Listener, Runnable {
    public static void create(Arena arena) {
        new ArenaChoosingInventory(arena);
    }

    private static Map<Character, ItemStack> PATTERN_ITEM = new HashMap<Character, ItemStack>() {{
        put('t', ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&r").data(9).build());
        put('b', ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&r").data(11).build());
    }};

    private static final char[][] PATTERN = {
            { 'b', 'b', 'b', 'b', 't', 'b', 'b', 'b', 'b' },
            { 'b', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'b' },
            { 't', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 't' },
            { 't', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 't' },
            { 'b', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'b' },
            { 'b', 'b', 'b', 'b', 't', 'b', 'b', 'b', 'b' },
    };

    private List<VotedTemplate> votedTemplates = new ArrayList<>();

    private Arena arena;
    private Inventory inventory;
    private int remainingSeconds = 3;
    private BukkitTask task;

    private ArenaChoosingInventory(Arena arena) {
        this.arena = arena;
        JavaPlugin plugin = GamePlugin.getProvidingPlugin(GamePlugin.class);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0, 20);

        this.inventory = Bukkit.createInventory(null, 6 * 9, MessageFormat.replaceColors("&eMapauswahl"));

        for (int x = 0; x < PATTERN.length; x++) {
            for (int y = 0; y < PATTERN[x].length; y++) {
                char code = PATTERN[x][y];
                if (code == 'x') continue;
                this.inventory.setItem(x * 9 + y, PATTERN_ITEM.get(code));
            }
        }
        List<ArenaTemplate> templates = GameManager
                .instance()
                .arenaContainer()
                .templateLoader()
                .templates();
        for (int i = 0; i < templates.size(); i++) {
            ArenaTemplate arenaTemplate = templates.get(i);
            int slot = i;
            if (slot < 2) slot += 2;
            if (slot > 6) slot += 4;
            if (i > 9) break;
            ItemStack itemStack = this.createItem(arenaTemplate);
            this.inventory.setItem(slot + 9 + 9, itemStack);
            this.votedTemplates.add(new VotedTemplate(slot + 9 + 9, itemStack, arenaTemplate, new ArrayList<>()));
        }

        arena.players().forEach(gamePlayer -> gamePlayer.player().openInventory(this.inventory));
    }

    private ItemStack createItem(ArenaTemplate template) {
        return ItemStackBuilder
                .of(Material.valueOf(template.displayMaterial().toUpperCase()))
                .name(ChatColor.YELLOW + template.name())
                .amount(1)
                .lore("", "&7Builder: &e" + template.description(), "&7Votes: &e0")
                .build();
    }

    @EventHandler
    public void handle(InventoryClickEvent event) {
        if (!this.inventory.equals(event.getClickedInventory())) return;
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        if (event.getCurrentItem() == null)
            return;
        PlayerProvider.find(event.getWhoClicked().getUniqueId()).ifPresent(gamePlayer -> {
            this.votedTemplates.forEach(votedTemplate -> votedTemplate.voters().remove(gamePlayer));
            this.votedTemplates.stream().filter(votedTemplate -> votedTemplate.itemStack.equals(event.getCurrentItem())).findAny().ifPresent(votedTemplate -> {
                votedTemplate.voters.add(gamePlayer);
                gamePlayer.sound(Sound.ORB_PICKUP, 1.2f);
                this.updateVotes();
            });
        });

    }

    private void updateVotes() {
        this.votedTemplates.forEach(votedTemplate -> {
            votedTemplate.itemStack = ItemStackBuilder.of(votedTemplate.itemStack).clearLore().lore("", "&7Builder: &e" + votedTemplate.template.description(), "&7Votes: &e" + votedTemplate.voters.size()).build();
            this.inventory.setItem(votedTemplate.slot, votedTemplate.itemStack);
        });
    }

    private VotedTemplate getMostVotedTemplate() {
        return this.votedTemplates.stream().max(Comparator.comparingInt(o -> o.voters.size())).orElse(null);
    }

    @Override
    public void run() {
        if (this.remainingSeconds == 0) {
            this.task.cancel();
            VotedTemplate votedTemplate = this.getMostVotedTemplate();
            votedTemplate.itemStack = ItemStackBuilder.of(votedTemplate.itemStack).hideAttributes().enchant(Enchantment.DURABILITY, 1).build();
            this.inventory.setItem(votedTemplate.slot, votedTemplate.itemStack);
            if (votedTemplate == null) {
                GameManager.instance().arenaContainer().abort(this.arena);
                return;
            }
            this.arena.template(votedTemplate.template);
            this.arena.players().forEach(gamePlayer -> gamePlayer.sound(Sound.LEVEL_UP, 1f));
            Bukkit.getScheduler().runTaskLaterAsynchronously(GamePlugin.getProvidingPlugin(GamePlugin.class),
                    () -> {
                        this.unregister();
                        GameManager.instance().arenaContainer().start(this.arena);
                    }, 30);
            return;
        }
        this.arena.players().forEach(gamePlayer -> gamePlayer.player().setLevel(this.remainingSeconds));
        this.remainingSeconds--;
    }

    private void unregister() {
        this.arena.players().forEach(gamePlayer -> gamePlayer.player().closeInventory());
        HandlerList.unregisterAll(this);
    }

    @AllArgsConstructor
    @Getter
    @Accessors(fluent = true)
    public class VotedTemplate {
        private int slot;
        private ItemStack itemStack;
        private ArenaTemplate template;
        private List<GamePlayer> voters;
    }
}
