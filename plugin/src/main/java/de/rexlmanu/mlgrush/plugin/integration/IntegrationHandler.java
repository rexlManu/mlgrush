package de.rexlmanu.mlgrush.plugin.integration;

import de.rexlmanu.mlgrush.plugin.integration.pluginstube.PluginStubeIntegration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IntegrationHandler {

  private static final List<GameIntegration> INTEGRATIONS = Arrays.asList(
    new PluginStubeIntegration()
  );

  private static List<GameIntegration> availableIntegrations() {
    return INTEGRATIONS.stream().filter(GameIntegration::isAvailable).collect(Collectors.toList());
  }

  public static void enableIntegration() {
    availableIntegrations().forEach(GameIntegration::onEnable);
  }

  public static void gameInitIntegration() {
    availableIntegrations().forEach(GameIntegration::onGameInit);
  }
}
