package de.rexlmanu.mlgrush.arenacreator;

import de.rexlmanu.mlgrush.arenacreator.command.ArenaCreatorCommand;
import de.rexlmanu.mlgrush.arenacreator.process.ArenaCreationProcess;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ArenaCreatorPlugin extends JavaPlugin implements Listener {

  public static final List<ArenaCreationProcess> ARENA_CREATION_PROCESSES = new ArrayList<>();

  @Override
  public void onEnable() {
    this.getDataFolder().mkdir();
    this.getCommand("arenacreator").setExecutor(new ArenaCreatorCommand());
  }
}
