package de.rexlmanu.mlgrush.plugin.database;

public class DatabaseFactory {

  public static DatabaseContext create(DatabaseType databaseType) {
    try {
      return databaseType.contextClass().getConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
      return null;
    }
  }

}
