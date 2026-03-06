package de.rexlmanu.mlgrush.plugin.database.file;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginStubeDatabase extends FileDatabase {

  private static PluginStubeDatabase instance;
  public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

  public PluginStubeDatabase() {
    instance = this;
  }

  public static PluginStubeDatabase instance() {
    return instance;
  }
}
