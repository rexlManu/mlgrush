package de.rexlmanu.mlgrush.plugin.database;

import de.rexlmanu.mlgrush.plugin.database.file.FileDatabase;
import de.rexlmanu.mlgrush.plugin.database.file.PluginStubeDatabase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
public enum DatabaseType {
  FILE(FileDatabase.class),
  PLUGINSTUBE(PluginStubeDatabase.class);

  private Class<? extends DatabaseContext> contextClass;
}
