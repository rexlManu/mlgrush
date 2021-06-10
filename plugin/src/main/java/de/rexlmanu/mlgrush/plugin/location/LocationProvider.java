package de.rexlmanu.mlgrush.plugin.location;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class LocationProvider {

  private File file;
  private FileConfiguration configuration;

  public LocationProvider(File dataFolder) {
    this.file = dataFolder.toPath().resolve("location.yml").toFile();
    this.configuration = YamlConfiguration.loadConfiguration(this.file);
    if (!this.file.exists()) this.save();
  }

  public void save() {
    try {
      this.configuration.save(this.file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Optional<Location> get(String key) {
    return Optional.ofNullable((Location) this.configuration.get(key));
  }

  public void set(String key, Location location) {
    this.configuration.set(key, location);
    this.save();
  }
}
