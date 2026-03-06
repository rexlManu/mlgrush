package de.rexlmanu.mlgrush.plugin.database;

import de.rexlmanu.mlgrush.plugin.database.file.FileDatabase;

public class DatabaseFactory {

  public static DatabaseContext create() {
    return new FileDatabase();
  }

}
