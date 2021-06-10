package de.rexlmanu.mlgrush.plugin.arena.template;

import de.rexlmanu.mlgrush.arenalib.ArenaFormat;
import de.rexlmanu.mlgrush.arenalib.ArenaTemplate;
import de.rexlmanu.mlgrush.plugin.GamePlugin;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
public class ArenaTemplateLoader {

  private List<ArenaTemplate> templates;
  private File directory;

  public ArenaTemplateLoader() {
    this.templates = new ArrayList<>();
    this.directory = new File(GamePlugin.getProvidingPlugin(GamePlugin.class).getDataFolder(), "arenas");
    this.directory.mkdir();

    Arrays.stream(this.directory.listFiles(pathname -> pathname.toString().endsWith("arena")))
      .map(file -> file.toPath())
      .map(path -> ArenaFormat.fromFile(path))
      .filter(arenaTemplate -> arenaTemplate.isPresent())
      .map(Optional::get)
      .forEach(this.templates::add);
  }
}
