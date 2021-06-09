package de.rexlmanu.mlgrush.plugin;

import de.rexlmanu.mlgrush.plugin.command.InventoryCommand;
import de.rexlmanu.mlgrush.plugin.command.MainCommand;
import de.rexlmanu.mlgrush.plugin.game.GameManager;
import de.rexlmanu.mlgrush.plugin.player.GamePlayerData;
import eu.miopowered.repository.Repository;
import eu.miopowered.repository.impl.GsonRepository;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Path;

@Accessors(fluent = true)
public class GamePlugin extends JavaPlugin {

    @Getter
    private Repository<GamePlayerData> repository;

    @Override
    public void onEnable() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();

        Path playersDirectory = this.getDataFolder().toPath().resolve("players");
        if (!playersDirectory.toFile().exists()) {
            playersDirectory.toFile().mkdir();
        }

        this.repository = GsonRepository.of(playersDirectory, GamePlayerData.class);

        // Creates the actually game that handles everything
        GameManager.create();

        PluginCommand command = this.getCommand("mlgrush");
        MainCommand executor = new MainCommand();
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        this.getCommand("inventory").setExecutor(new InventoryCommand());
    }

    @Override
    public void onDisable() {
        GameManager.instance().onDisable();
    }
}
